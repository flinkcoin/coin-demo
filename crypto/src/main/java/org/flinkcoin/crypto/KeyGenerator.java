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

import org.flinkcoin.helper.helpers.ByteHelper;
import java.math.BigInteger;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import java.util.Arrays;

public class KeyGenerator {

    private static final EdDSAParameterSpec ED25519SPEC = EdDSANamedCurveTable.getByName("ed25519");

    private static final byte[] HMAC_SALT = "ed25519 seed".getBytes();

    public static final String MASTER_PATH = "m";

    public static final KeyPair getKeyPairFromSeed(byte[] seed) {

        byte[] I;
        I = HmacSha512.hmac512(seed, HMAC_SALT);

        //split into left/right
        byte[] IL = Arrays.copyOfRange(I, 0, 32);
        byte[] IR = Arrays.copyOfRange(I, 32, 64);

        EdDSAPrivateKey prk = new EdDSAPrivateKey(new EdDSAPrivateKeySpec(IL, ED25519SPEC));
        EdDSAPublicKey puk = new EdDSAPublicKey(new EdDSAPublicKeySpec(prk.getA(), ED25519SPEC));
        
       

        PrivateKey privateKey = new PrivateKey(prk, IL);
        privateKey.setDepth(0);
        privateKey.setChildNumber(new byte[]{0, 0, 0, 0});
        privateKey.setChainCode(IR);
        privateKey.setKeyData(ByteHelper.append(new byte[]{0}, IL));

        PublicKey publicKey = new PublicKey(puk, ByteHelper.append(new byte[]{0}, puk.getAbyte()));
        publicKey.setDepth(0);
        publicKey.setChildNumber(new byte[]{0, 0, 0, 0});
        publicKey.setChainCode(IR);

        KeyPair keyPair = new KeyPair(privateKey, publicKey, MASTER_PATH);
        return keyPair;
    }

    public static KeyPair deriveKeypair(KeyPair parent, long child, boolean isHardened) throws CryptoException {

        child += 0x80000000;

        byte[] xChain = parent.getPrivateKey().getChainCode();
        ///backwards hmac order in method?
        byte[] I;

        //If so (hardened child): let I = HMAC-SHA512(Key = cpar, Data = 0x00 || ser256(kpar) || ser32(i)). (Note: The 0x00 pads the private key to make it 33 bytes long.)
        BigInteger kpar = ByteHelper.parse256(parent.getPrivateKey().getKeyData());
        byte[] data = ByteHelper.append(new byte[]{0}, ByteHelper.ser256(kpar));
        data = ByteHelper.append(data, ByteHelper.ser32(child));
        I = HmacSha512.hmac512(data, xChain);

        //split into left/right
        byte[] IL = Arrays.copyOfRange(I, 0, 32);
        byte[] IR = Arrays.copyOfRange(I, 32, 64);

        byte[] childNumber = ByteHelper.ser32(child);

        EdDSAPrivateKey prk = new EdDSAPrivateKey(new EdDSAPrivateKeySpec(IL, ED25519SPEC));
        EdDSAPublicKey puk = new EdDSAPublicKey(new EdDSAPublicKeySpec(prk.getA(), ED25519SPEC));

        PrivateKey privateKey = new PrivateKey(prk, IL);
        privateKey.setDepth(parent.getPrivateKey().getDepth() + 1);
        privateKey.setChildNumber(childNumber);
        privateKey.setChainCode(IR);
        privateKey.setKeyData(ByteHelper.append(new byte[]{0}, IL));

        PublicKey publicKey = new PublicKey(puk, ByteHelper.append(new byte[]{0}, puk.getAbyte()));
        publicKey.setDepth(parent.getPublicKey().getDepth() + 1);
        publicKey.setChildNumber(childNumber);
        publicKey.setChainCode(IR);

        KeyPair keyPair = new KeyPair(privateKey, publicKey, getPath(parent.getPath(), child, isHardened));

        return keyPair;
    }

    private static String getPath(String parentPath, long child, boolean isHardened) {
        if (parentPath == null) {
            parentPath = MASTER_PATH;
        }
        return parentPath + "/" + child + (isHardened ? "'" : "");
    }
}
