package org.flinkcoin.crypto;

/*-
 * #%L
 * Flink - Crypto
 * %%
 * Copyright (C) 2021 - 2022 Flink Foundation
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.bouncycastle.crypto.digests.RIPEMD160Digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Basic hash functions
 */
public class HashHelper {

    /**
     * SHA-256
     *
     * @param input input
     * @return sha256(input)
     */
    public static byte[] sha256(byte[] input) throws CryptoException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Unable to find SHA-256", e);
        }
    }

    /**
     * SHA-512
     *
     * @param input input
     * @return sha512(input)
     */
    public static byte[] sha512(byte[] input) throws CryptoException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Unable to find SHA-512", e);
        }
    }

    /**
     * sha256(sha256(bytes))
     *
     * @param bytes input
     * @return sha'd twice result
     */
    public static byte[] sha256Twice(byte[] bytes) throws CryptoException {
        return sha256Twice(bytes, 0, bytes.length);
    }

    public static byte[] sha256Twice(final byte[] bytes, final int offset, final int length) throws CryptoException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(bytes, offset, length);
            digest.update(digest.digest());
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Unable to find SHA-256", e);
        }
    }

    /**
     * H160
     *
     * @param input input
     * @return h160(input)
     */
    public static byte[] h160(byte[] input) throws CryptoException {
        byte[] sha256 = sha256(input);

        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(sha256, 0, sha256.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
    }
}
