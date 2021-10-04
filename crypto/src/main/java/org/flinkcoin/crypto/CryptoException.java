package org.flinkcoin.crypto;

import java.io.Serializable;

public class CryptoException extends Exception implements Serializable {

    public static final long serialVersionUID = 1L;

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoException(Throwable cause) {
        super(cause);
    }
}
