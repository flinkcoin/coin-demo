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

public class Key {

    private int depth;
    private byte[] childNumber;
    private byte[] chainCode;
    private byte[] keyData;

    Key(byte[] version, int depth,  byte[] childNumber, byte[] chainCode, byte[] keyData) {
        this.depth = depth;
        this.childNumber = childNumber;
        this.chainCode = chainCode;
        this.keyData = keyData;
    }

    Key() {
    }


    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setChildNumber(byte[] childNumber) {
        this.childNumber = childNumber;
    }

    public void setChainCode(byte[] chainCode) {
        this.chainCode = chainCode;
    }

    public void setKeyData(byte[] keyData) {
        this.keyData = keyData;
    }

    public byte[] getChainCode() {
        return chainCode;
    }

    /**
     * Get the full chain key. This is not the public/private key for the address.
     *
     * @return full HD Key
     */
//s
    public int getDepth() {
        return depth;
    }

    public byte[] getKeyData() {
        return keyData;
    }
}
