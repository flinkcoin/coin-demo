package org.flinkcoin.crypto;

import net.i2p.crypto.eddsa.EdDSAPublicKey;

public class PublicKey extends Key {

    private final EdDSAPublicKey edDSAPublicKey;
    private final byte[] publicKey;

    public PublicKey(EdDSAPublicKey edDSAPublicKey, byte[] publicKey) {
        this.edDSAPublicKey = edDSAPublicKey;
        this.publicKey = publicKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public EdDSAPublicKey getEdDSAPublicKey() {
        return edDSAPublicKey;
    }

}
