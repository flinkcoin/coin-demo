package org.flinkcoin.crypto;

public class KeyPair {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String path;

    public KeyPair(PrivateKey privateKey, PublicKey publicKey, String path) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.path = path;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getPath() {
        return path;
    }
}
