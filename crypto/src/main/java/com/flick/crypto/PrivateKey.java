package com.flick.crypto;

import net.i2p.crypto.eddsa.EdDSAPrivateKey;

public class PrivateKey extends Key {

    private final EdDSAPrivateKey edDSAPrivateKey;
    private final byte[] privateKey;

    public PrivateKey(EdDSAPrivateKey edDSAPrivateKey, byte[] privateKey) {
        this.edDSAPrivateKey = edDSAPrivateKey;
        this.privateKey = privateKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public EdDSAPrivateKey getEdDSAPrivateKey() {
        return edDSAPrivateKey;
    }

}
