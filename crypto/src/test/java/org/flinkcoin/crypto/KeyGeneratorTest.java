/*
 * Copyright © 2021 Flink Foundation (info@flinkcoin.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flinkcoin.crypto;

import java.io.UnsupportedEncodingException;
import java.security.spec.X509EncodedKeySpec;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.Utils;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KeyGeneratorTest {

    public static final byte[] SEED = Hex.decode("000102030405060708090a0b0c0d0e0f");

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testGetKeyPairFromSeed() {
        byte[] fingerprint = Hex.decode("00000000");
        byte[] chainCode = Hex.decode("90046a93de5380a72b5e45010748567d5ea02bbf6522f979e05c0d8d8ca9fffb");
        byte[] privateKey = Hex.decode("2b4be7f19ee27bbf30c667b642d5f4aa69fd169872f8fc3059c08ebae2eb19e7");
        byte[] publicKey = Hex.decode("00a4b2856bfec510abab89753fac1ac0e1112364e7d250545963f135f2a33188ed");
        final KeyPair keyPair = KeyGenerator.getKeyPairFromSeed(SEED);

        assertArrayEquals(chainCode, keyPair.getPrivateKey().getChainCode());
        assertArrayEquals(privateKey, keyPair.getPrivateKey().getPrivateKey());
        assertArrayEquals(publicKey, keyPair.getPublicKey().getPublicKey());

    }

    @Test
    public void testVector0H1H() throws UnsupportedEncodingException, CryptoException {

        byte[] chainCode = Hex.decode("a320425f77d1b5c2505a6b1b27382b37368ee640e3557c315416801243552f14");
        byte[] privateKey = Hex.decode("b1d0bad404bf35da785a64ca1ac54b2617211d2777696fbffaf208f746ae84f2");
        byte[] publicKey = Hex.decode("001932a5270f335bed617d5b935c80aedb1a35bd9fc1e31acafd5372c30f5c1187");

        KeyPair master = KeyGenerator.getKeyPairFromSeed(SEED);
        KeyPair address = KeyGenerator.deriveKeypair(master, 0, true);
        address = KeyGenerator.deriveKeypair(address, 1, true);

//        assertArrayEquals(chainCode, address.getPrivateKey().getChainCode());
        assertArrayEquals(privateKey, address.getPrivateKey().getPrivateKey());
        assertArrayEquals(publicKey, address.getPublicKey().getPublicKey());

    }

    @Test
    public void testVector0H1H2H() throws UnsupportedEncodingException, CryptoException {
        byte[] fingerprint = Hex.decode("ebe4cb29");
        byte[] chainCode = Hex.decode("2e69929e00b5ab250f49c3fb1c12f252de4fed2c1db88387094a0f8c4c9ccd6c");
        byte[] privateKey = Hex.decode("92a5b23c0b8a99e37d07df3fb9966917f5d06e02ddbd909c7e184371463e9fc9");
        byte[] publicKey = Hex.decode("00ae98736566d30ed0e9d2f4486a64bc95740d89c7db33f52121f8ea8f76ff0fc1");

        KeyPair master = KeyGenerator.getKeyPairFromSeed(SEED);
        KeyPair address = KeyGenerator.deriveKeypair(master, 0, true);
        address = KeyGenerator.deriveKeypair(address, 1, true);
        address = KeyGenerator.deriveKeypair(address, 2, true);

//        assertArrayEquals(chainCode, address.getPrivateKey().getChainCode());
        assertArrayEquals(privateKey, address.getPrivateKey().getPrivateKey());
        assertArrayEquals(publicKey, address.getPublicKey().getPublicKey());

    }

    static final byte[] TEST_PUBKEY = Utils.hexToBytes("302a300506032b657003210019bf44096984cdfe8541bac167dc3b96c85086aa30b6b6cb0c5c38ad703166e1");

    static final byte[] TEST_PUBKEY_NULL_PARAMS = Utils.hexToBytes("302c300706032b6570050003210019bf44096984cdfe8541bac167dc3b96c85086aa30b6b6cb0c5c38ad703166e1");
    static final byte[] TEST_PUBKEY_OLD = Utils.hexToBytes("302d300806032b65640a010103210019bf44096984cdfe8541bac167dc3b96c85086aa30b6b6cb0c5c38ad703166e1");

    @Test
    public void testDecodeAndEncode() throws Exception {
        // Decode
        X509EncodedKeySpec encoded = new X509EncodedKeySpec(TEST_PUBKEY);
        EdDSAPublicKey keyIn = new EdDSAPublicKey(encoded);

        // Encode
        EdDSAPublicKeySpec decoded = new EdDSAPublicKeySpec(
                keyIn.getA(),
                keyIn.getParams());

        byte[] abyte = keyIn.getAbyte();
        EdDSAPublicKey keyOut = new EdDSAPublicKey(decoded);
        abyte = keyOut.getAbyte();

 

        // Check
        assertArrayEquals(keyIn.getEncoded(), TEST_PUBKEY);
    }

}
