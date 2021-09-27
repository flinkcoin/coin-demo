package com.flick.node.storage;

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
