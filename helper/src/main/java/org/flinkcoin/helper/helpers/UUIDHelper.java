package org.flinkcoin.helper.helpers;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDHelper {

    public static String asString() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static byte[] asBytes() {

        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
