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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HmacSha512 {

    private static final String HMAC_SHA512 = "HmacSHA512";

    public static byte[] hmac512(byte[] key, byte[] seed) {
        try {
            Mac sha512_HMAC = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec keySpec = new SecretKeySpec(seed, HMAC_SHA512);
            sha512_HMAC.init(keySpec);
            return sha512_HMAC.doFinal(key);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Unable to perform hmac512.", e);
        }
    }
}
