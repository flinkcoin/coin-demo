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

public class BitHelper {

    /**
     * Checks bit value from the left, 1 based
     *
     * @param data  data
     * @param index index to check
     * @return true if set
     */
    public static boolean checkBit(byte data, int index) {
        byte bit = (byte) ((data >> (8 - index)) & 1);
        return bit == 0x1;
    }

    /**
     * Set a bit of a byte
     *
     * @param data  data
     * @param index index to set
     * @return byte with bit set
     */
    public static byte setBit(byte data, int index) {
        data |= 1 << (8 - index);
        return data;
    }

    /**
     * Unset a bit of a byte
     *
     * @param data  data
     * @param index index to clear
     * @return byte with bit unset
     */
    public static byte unsetBit(byte data, int index) {
        data &= ~(1 << (8 - index));
        return data;
    }
}
