package org.flinkcoin.crypto.mnemonic;

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

import org.flinkcoin.crypto.CryptoException;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MnemonicGeneratorTest {

    @Test
    public void testHappyPath() throws CryptoException {

        Dictionary dictionary;
        try {
            dictionary = new Dictionary(Language.english);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unknown dictionary");
        }

        MnemonicGenerator generator = new MnemonicGenerator();

        String phrase = generator.getWordlist(128, Language.english);

        byte[] seed = generator.getSeedFromWordlist(phrase, "", Language.english);
        byte[] seedAgain = generator.getSeedFromWordlist(phrase, "", Language.english);
        Assertions.assertArrayEquals(seed, seedAgain);

        String[] words = phrase.split(" ");

        int index = dictionary.indexOf(words[0]);

        try {
            words[0] = "asdf";
            generator.getSeedFromWordlist(String.join(" ", words), "", Language.english);
            Assertions.fail("Should not allow unknown word");
        } catch (IllegalArgumentException e) {
        }

//        try {
//            words[0] = dictionary.getWord((index + 1) % 2048);
//            generator.getSeedFromWordlist(String.join(" ", words), "", Language.english);
//            Assert.fail("Should not allow non-checksum'd words");
//        } catch (IllegalArgumentException e) {
//
//        }
    }
}
