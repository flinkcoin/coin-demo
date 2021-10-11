package org.flinkcoin.crypto;

public class Key {

    private int depth;
    private byte[] childNumber;
    private byte[] chainCode;
    private byte[] keyData;

    Key(byte[] version, int depth,  byte[] childNumber, byte[] chainCode, byte[] keyData) {
        this.depth = depth;
        this.childNumber = childNumber;
        this.chainCode = chainCode;
        this.keyData = keyData;
    }

    Key() {
    }


    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setChildNumber(byte[] childNumber) {
        this.childNumber = childNumber;
    }

    public void setChainCode(byte[] chainCode) {
        this.chainCode = chainCode;
    }

    public void setKeyData(byte[] keyData) {
        this.keyData = keyData;
    }

    public byte[] getChainCode() {
        return chainCode;
    }

    /**
     * Get the full chain key. This is not the public/private key for the address.
     *
     * @return full HD Key
     */
//s
    public int getDepth() {
        return depth;
    }

    public byte[] getKeyData() {
        return keyData;
    }
}
