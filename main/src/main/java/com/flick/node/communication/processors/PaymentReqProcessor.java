package com.flick.node.communication.processors;

import com.flick.data.proto.api.Api;
import com.flick.data.proto.common.Common.PaymentRequest;
import com.flick.node.communication.*;
import com.flick.data.proto.communication.Message;
import com.flick.data.proto.communication.Message.PaymentReq;
import org.flinkcoin.helper.helpers.DateHelper;
import com.flick.node.api.AccountServiceImpl;
import com.flick.node.handlers.IdHandler;
import com.flick.node.managers.CryptoManager;
import com.flick.node.services.FloodService;
import com.google.protobuf.Any;
import io.netty.channel.ChannelHandlerContext;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PaymentReqProcessor extends BaseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentReqProcessor.class);

    private final CryptoManager cryptoManager;
    private final IdHandler idHandler;
    private final FloodService messageService;
    private final Provider<AccountServiceImpl> accountServiceImpl;

    @Inject
    public PaymentReqProcessor(CryptoManager cryptoManager, IdHandler idHandler, FloodService messageService, Provider<AccountServiceImpl> accountServiceImpl) {
        this.cryptoManager = cryptoManager;
        this.idHandler = idHandler;
        this.messageService = messageService;
        this.accountServiceImpl = accountServiceImpl;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ChannelData channelData, Message req) throws Exception {
        Any any = req.getAny();
        PaymentReq paymentReq = any.unpack(PaymentReq.class);

        if (idHandler.checkExists(paymentReq.getBody().getMsgId())) {
            return;
        } else {
            idHandler.putId(paymentReq.getBody().getMsgId(), DateHelper.dateNow().getTime());
        }

        PaymentReq.Body body = paymentReq.getBody();

        if (!cryptoManager.verifyData(body.getNodeId(), body.toByteString(), paymentReq.getSignature())) {
            LOGGER.info("Wrong signtaure!");
            return;
        }

        messageService.newMessage(req);

        Api.InfoRes infoRes = Api.InfoRes.newBuilder()
                .setInfoType(Api.InfoRes.InfoType.PAYMENT_REQUEST)
                .setPaymentRequest(paymentReq.getBody().getPaymentRequest())
                .setAccountId(body.getPaymentRequest().getToAccountId())
                .build();
        accountServiceImpl.get().sentInfo(infoRes);
    }

}
