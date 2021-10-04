package org.flinkcoin.data;

import java.util.HashMap;
import java.util.Map;

public enum MessageType {
    I_AM_ALIVE("type.googleapis.com/org.flinkcoin.data.proto.communication.Message.IAmAlive"),
    AUTHENTICATION_REQ("type.googleapis.com/org.flinkcoin.data.proto.communication.Message.AuthenticationReq"),
    AUTHENTICATION_RES("type.googleapis.com/org.flinkcoin.data.proto.communication.Message.AuthenticationRes"),
    NODE_PUB("type.googleapis.com/org.flinkcoin.data.proto.communication.Message.NodePub"),
    BLOCK_PUB("type.googleapis.com/org.flinkcoin.data.proto.communication.Message.BlockPub"),
    BLOCK_CONFIRM_PUB("type.googleapis.com/org.flinkcoin.data.proto.communication.Message.BlockConfirmPub"),
    BLOCK_VERIFY_PUB("type.googleapis.com/org.flinkcoin.data.proto.communication.Message.BlockVerifyPub"),
    BLOCK_VERIFY_CONFIRM_PUB("type.googleapis.com/org.flinkcoin.data.proto.communication.Message.BlockVerifyConfirmPub"),
    BLOCK_REQ("type.googleapis.com/org.flinkcoin.data.proto.communication.Message.BlockReq"),
    BLOCK_RES("type.googleapis.com/org.flinkcoin.data.proto.communication.Message.BlockRes"),
    PAYMENT_REQ("type.googleapis.com/org.flinkcoin.data.proto.communication.Message.PaymentReq");

    private static final Map<String, MessageType> MAP = new HashMap<>();

    static {
        for (MessageType mt : MessageType.values()) {
            MAP.put(mt.getTypeUrl(), mt);
        }
    }

    private final String typeUrl;

    private MessageType(String typeUrl) {
        this.typeUrl = typeUrl;
    }

    public String getTypeUrl() {
        return typeUrl;
    }

    public static MessageType fromTypeUrl(String typeUrl) {
        MessageType messageType = MAP.get(typeUrl);

        if (messageType == null) {
            throw new IllegalArgumentException("Wrong message type!");
        }

        return messageType;
    }
}
