package org.flinkcoin.crypto.mnemonic;

/*-
 * #%L
 * Flink - Crypto
 * %%
 * Copyright (C) 2021 Flink Foundation
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Dictionary {

    private List<String> words = new ArrayList<>();

    public Dictionary(Language language) throws IOException {

        InputStream wordStream = this.getClass().getClassLoader().getResourceAsStream("wordlists/" + language.name() + ".txt");

        BufferedReader reader = new BufferedReader(new InputStreamReader(wordStream));
        String word;

        while ((word = reader.readLine()) != null) {
            words.add(word);
        }
    }

    public String getWord(int wordIdx) {
        return words.get(wordIdx);
    }

    public int indexOf(String word) {
        return words.indexOf(word);
    }
}
