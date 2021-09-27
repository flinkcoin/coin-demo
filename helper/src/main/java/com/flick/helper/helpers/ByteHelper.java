package com.flick.helper.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

public class ByteHelper {

    /**
     * ser32(i): serialize a 32-bit unsigned integer i as a 4-byte sequence, most significant byte first.
     * <p>
     * Prefer long type to hold unsigned ints.
     *
     * @return ser32(i)
     */
    public static byte[] ser32(long i) {

        byte[] ser = new byte[4];
        ser[0] = (byte) (i >> 24);
        ser[1] = (byte) (i >> 16);
        ser[2] = (byte) (i >> 8);
        ser[3] = (byte) (i);
        return ser;
    }

    /**
     * ser256(p): serializes the integer p as a 32-byte sequence, most significant byte first.
     *
     * @param p big integer
     * @return 32 byte sequence
     */
    public static byte[] ser256(BigInteger p) {
        byte[] byteArray = p.toByteArray();
        byte[] ret = new byte[32];

        //0 fill value
        Arrays.fill(ret, (byte) 0);

        //copy the bigint in
        if (byteArray.length <= ret.length) {
            System.arraycopy(byteArray, 0, ret, ret.length - byteArray.length, byteArray.length);
        } else {
            System.arraycopy(byteArray, byteArray.length - ret.length, ret, 0, ret.length);
        }

        return ret;
    }

    /**
     * ser256(p): serializes the integer p as a 32-byte sequence, least significant byte first.
     *
     * @param p big integer
     * @return 32 byte sequence
     */
    public static byte[] ser256LE(BigInteger p) {

        byte[] byteArray = p.toByteArray();
        reverse(byteArray);

        byte[] ret = new byte[32];

        //0 fill value
        Arrays.fill(ret, (byte) 0);

        //copy the bigint in
        if (byteArray.length <= ret.length) {
            System.arraycopy(byteArray, 0, ret, ret.length - byteArray.length, byteArray.length);
        } else {
            System.arraycopy(byteArray, byteArray.length - ret.length, ret, 0, ret.length);
        }

        return ret;
    }

    /**
     * parse256(p): interprets a 32-byte sequence as a 256-bit number, most significant byte first.
     *
     * @param p bytes
     * @return 256 bit number
     */
    public static BigInteger parse256(byte[] p) {
        return new BigInteger(1, p);
    }

    /**
     * parse256LE(p): interprets a 32-byte sequence as a 256-bit number, least significant byte first
     *
     * @param p bytes
     * @return 256 bit number
     */
    public static BigInteger parse256LE(byte[] p) {
        byte[] copy = Arrays.copyOf(p, p.length);
        reverse(copy);

        return new BigInteger(1, copy);
    }

    static void reverse(byte a[]) {
        int n = a.length;
        byte[] b = new byte[n];
        int j = n;
        for (int i = 0; i < n; i++) {
            b[j - 1] = a[i];
            j = j - 1;
        }

        for (int k = 0; k < n; k++) {
            a[k] = b[k];
        }
    }

    /**
     * Append two byte arrays
     *
     * @param a first byte array
     * @param b second byte array
     * @return bytes appended
     */
    public static byte[] append(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] ser32LE(long i) {
        byte[] ser = new byte[4];
        ser[3] = (byte) (i >> 24);
        ser[2] = (byte) (i >> 16);
        ser[1] = (byte) (i >> 8);
        ser[0] = (byte) (i);
        return ser;
    }

    public static byte[] toByteArray(Object obj) throws IOException {
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return bytes;
    }

    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }

    public static String toString(byte[] bytes) {
        return new String(bytes);
    }

    public static byte[] longToBytes(long aLong) {
        byte[] array = new byte[8];

        array[7] = (byte) (aLong & 0xff);
        array[6] = (byte) ((aLong >> 8) & 0xff);
        array[5] = (byte) ((aLong >> 16) & 0xff);
        array[4] = (byte) ((aLong >> 24) & 0xff);
        array[3] = (byte) ((aLong >> 32) & 0xff);
        array[2] = (byte) ((aLong >> 40) & 0xff);
        array[1] = (byte) ((aLong >> 48) & 0xff);
        array[0] = (byte) ((aLong >> 56) & 0xff);

        return array;
    }

    public static byte[] intToBytes(int anInt) {
        byte[] array = new byte[4];

        array[3] = (byte) (anInt & 0xff);
        array[2] = (byte) ((anInt >> 8) & 0xff);
        array[1] = (byte) ((anInt >> 16) & 0xff);
        array[0] = (byte) ((anInt >> 24) & 0xff);

        return array;
    }

    public static byte[] shortToBytes(short aShort) {
        byte[] array = new byte[2];

        array[1] = (byte) (aShort & 0xff);
        array[0] = (byte) ((aShort >> 8) & 0xff);

        return array;
    }

    public static long bytesToLong(byte[] bytes, int pos) {
        if (bytes.length + pos < 8) {
            throw new IllegalArgumentException("a long can only be decoded from 8 bytes of an array (got a " + bytes.length + " byte(s) array, must start at position " + pos + ")");
        }

        long result = 0;

        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result ^= (long) bytes[i + pos] & 0xFF;
        }

        return result;
    }

    public static int bytesToInt(byte[] bytes, int pos) {
        if (bytes.length + pos < 4) {
            throw new IllegalArgumentException("an integer can only be decoded from 4 bytes of an array (got a " + bytes.length + " byte(s) array, must start at position " + pos + ")");
        }

        int result = 0;

        for (int i = 0; i < 4; i++) {
            result <<= 8;
            result ^= (int) bytes[i + pos] & 0xFF;
        }

        return result;
    }

    public static String byteArrayToHexString(byte[] uid) {
        char[] hexChars = new char[uid.length * 2];
        int c = 0;
        int v;
        for (int i = 0; i < uid.length; i++) {
            v = uid[i] & 0xFF;
            hexChars[c++] = HEX[v >> 4];
            hexChars[c++] = HEX[v & 0xF];
        }
        return new String(hexChars);
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static void hexStringToByteArray(Optional<String> rawBlock) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
