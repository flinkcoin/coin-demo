package org.flinkcoin.node.communication.processors;

import org.flinkcoin.data.proto.api.Api;
import org.flinkcoin.data.proto.common.Common.PaymentRequest;
import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.data.proto.communication.Message.PaymentReq;
import org.flinkcoin.helper.helpers.DateHelper;
import org.flinkcoin.node.api.AccountServiceImpl;
import org.flinkcoin.node.handlers.IdHandler;
import org.flinkcoin.node.managers.CryptoManager;
import org.flinkcoin.node.services.FloodService;
import com.google.protobuf.Any;
import io.netty.channel.ChannelHandlerContext;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.flinkcoin.node.communication.BaseProcessor;
import org.flinkcoin.node.communication.ChannelData;
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
