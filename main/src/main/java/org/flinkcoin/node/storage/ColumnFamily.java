package org.flinkcoin.node.storage;

/*-
 * #%L
 * Flink - Node
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

public enum ColumnFamily {
    ACCOUNT(1, "account"),
    ACCOUNT_UNCLAIMED(2, "account_unclaimed"),
    BLOCK(3, "block"),
    CLAIMED_BLOCK(4, "claimed_block"),
    UNCLAIMED_INFO_BLOCK(5, "unclaimed_info_block"),
    UNCLAIMED_BLOCK(6, "unclaimed_block"),
    WEIGHT(7, "weight"),
    NODE(8, "node"),
    NODE_ADDRESS(9, "node_address");

    private final int id;
    private final String name;

    private ColumnFamily(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
