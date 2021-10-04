package org.flinkcoin.node.api;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.flinkcoin.data.proto.api.AccountServiceGrpc.AccountServiceImplBase;
import org.flinkcoin.data.proto.api.Api;
import org.flinkcoin.data.proto.api.Api.AccountCountReq;
import org.flinkcoin.data.proto.api.Api.AccountCountRes;
import org.flinkcoin.data.proto.api.Api.GetBlockReq;
import org.flinkcoin.data.proto.api.Api.GetBlockRes;
import org.flinkcoin.data.proto.api.Api.InfoRes;
import org.flinkcoin.data.proto.api.Api.LastBlockReq;
import org.flinkcoin.data.proto.api.Api.LastBlockRes;
import org.flinkcoin.data.proto.api.Api.ListBlockReq;
import org.flinkcoin.data.proto.api.Api.ListBlockRes;
import org.flinkcoin.data.proto.api.Api.ListUnclaimedBlockReq;
import org.flinkcoin.data.proto.api.Api.ListUnclaimedBlockRes;
import org.flinkcoin.data.proto.common.Common;
import org.flinkcoin.data.proto.common.Common.PaymentRequest;
import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.helper.Pair;
import org.flinkcoin.helper.helpers.DateHelper;
import org.flinkcoin.helper.helpers.UUIDHelper;
import org.flinkcoin.node.caches.AccountCache;
import org.flinkcoin.node.caches.AccountUnclaimedCache;
import org.flinkcoin.node.caches.BlockCache;
import org.flinkcoin.node.caches.UnclaimedBlockCache;
import org.flinkcoin.node.communication.BaseProcessor;
import org.flinkcoin.node.handlers.IdHandler;
import org.flinkcoin.node.managers.CryptoManager;
import org.flinkcoin.node.managers.NodeManager;
import org.flinkcoin.node.services.BlockService;
import org.flinkcoin.node.services.FloodService;
import org.flinkcoin.node.storage.ColumnFamily;
import org.flinkcoin.node.storage.Storage;
import static org.flinkcoin.node.storage.Storage.NULL_HASH;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AccountServiceImpl extends AccountServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final Map<Integer, StreamObserver<Api.InfoRes>> infoObservers = new ConcurrentHashMap<>();

    private final Storage storage;
    private final BlockService blockService;
    private final FloodService messageService;
    private final IdHandler idHandler;
    private final NodeManager nodeManager;
    private final CryptoManager cryptoManager;
    private final BlockCache blockCache;
    private final AccountCache accountCache;
    private final AccountUnclaimedCache accountUnclaimedCache;
    private final UnclaimedBlockCache unclaimedBlockCache;

    @Inject
    public AccountServiceImpl(Storage storage, BlockService blockService, FloodService messageService, IdHandler idHandler, NodeManager nodeManager,
            CryptoManager cryptoManager, BlockCache blockCache, AccountCache accountCache, AccountUnclaimedCache accountUnclaimedCache, UnclaimedBlockCache unclaimedBlockCache) {
        this.storage = storage;
        this.blockService = blockService;
        this.messageService = messageService;
        this.idHandler = idHandler;
        this.nodeManager = nodeManager;
        this.cryptoManager = cryptoManager;
        this.blockCache = blockCache;
        this.accountCache = accountCache;
        this.accountUnclaimedCache = accountUnclaimedCache;
        this.unclaimedBlockCache = unclaimedBlockCache;
    }

    @Override
    public void numAccounts(AccountCountReq request, StreamObserver<AccountCountRes> responseObserver) {

        AccountCountRes.Builder accountResBuilder = AccountCountRes.newBuilder();

        long count = 0;
        try {
            count = storage.count(ColumnFamily.ACCOUNT);
        } catch (RocksDBException ex) {
            LOGGER.error("Not ok", ex);
        }

        accountResBuilder.setCount(count);
        responseObserver.onNext(accountResBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getBlock(GetBlockReq request, StreamObserver<GetBlockRes> responseObserver) {

        GetBlockRes.Builder accountResBuilder = GetBlockRes.newBuilder();

        Optional<Common.FullBlock> block = blockCache.getBlock(request.getBlockHash());

        if (block.isPresent()) {
            accountResBuilder.setBlock(block.get().getBlock());
        }

        responseObserver.onNext(accountResBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void lastBlock(LastBlockReq request, StreamObserver<LastBlockRes> responseObserver) {

        Optional<ByteString> lastBlockHash = accountCache.getLastBlockHash(request.getAccountId());
        LastBlockRes.Builder accountResBuilder = LastBlockRes.newBuilder();

        if (lastBlockHash.isPresent()) {
            Optional<Common.FullBlock> block = blockCache.getBlock(lastBlockHash.get());

            if (block.isPresent()) {
                accountResBuilder.setBlock(block.get().getBlock());
            }
        }

        responseObserver.onNext(accountResBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listBlocks(ListBlockReq request, StreamObserver<ListBlockRes> responseObserver) {

        Optional<ByteString> lastBlockHash = accountCache.getLastBlockHash(request.getAccountId());
        ListBlockRes.Builder accountResBuilder = ListBlockRes.newBuilder();
        if (lastBlockHash.isPresent()) {
            Optional<Common.FullBlock> block = blockCache.getBlock(lastBlockHash.get());

            for (int i = 0; i < request.getNum(); i++) {

                if (block.isEmpty()) {
                    break;
                }

                accountResBuilder.addBlock(block.get().getBlock());

                block = blockCache.getBlock(block.get().getBlock().getBody().getPreviousBlockHash());
            }
        }

        responseObserver.onNext(accountResBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listUnclaimedBlocks(ListUnclaimedBlockReq request, StreamObserver<ListUnclaimedBlockRes> responseObserver) {

        Optional<ByteString> lastBlockHash = accountUnclaimedCache.getLastBlockHash(request.getAccountId());
        ListUnclaimedBlockRes.Builder accountResBuilder = ListUnclaimedBlockRes.newBuilder();

        if (lastBlockHash.isPresent()) {
            List<ByteString> blockHashes = new ArrayList<>();

            blockHashes.add(lastBlockHash.get());

            Optional<ByteString> lastUnclaimedBlock = unclaimedBlockCache.getLastUnclaimedBlock(lastBlockHash.get());

            int count = 0;
            while (lastUnclaimedBlock.isPresent() && !lastUnclaimedBlock.get().equals(NULL_HASH) && count < request.getNum()) {
                blockHashes.add(lastUnclaimedBlock.get());
                lastUnclaimedBlock = unclaimedBlockCache.getLastUnclaimedBlock(lastUnclaimedBlock.get());
                count++;
            }

            for (ByteString blockHash : blockHashes) {
                Optional<Common.FullBlock> block = blockCache.getBlock(blockHash);

                if (block.isEmpty()) {
                    break;
                }

                accountResBuilder.addBlock(block.get().getBlock());
            }
        }

        responseObserver.onNext(accountResBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void paymentRequest(Api.PaymentTransactionReq request, StreamObserver<Api.PaymentTransactionRes> responseObserver) {

        Api.PaymentTransactionRes.Builder transactionResBuilder = Api.PaymentTransactionRes.newBuilder();

        Common.PaymentRequest paymentRequest = request.getPaymentRequest();

        try {
            publishPaymentRequest(paymentRequest);
            transactionResBuilder.setSuccess(true);
        } catch (Exception ex) {
            LOGGER.error("Something is wrong!", ex);
        }

        responseObserver.onNext(transactionResBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void transaction(Api.TransactionReq request, StreamObserver<Api.TransactionRes> responseObserver) {

        Api.TransactionRes.Builder transactionResBuilder = Api.TransactionRes.newBuilder();

        Common.Block block = request.getBlock();

        try {
            publish(block);
            transactionResBuilder.setSuccess(true);
        } catch (Exception ex) {
            LOGGER.error("Something is wrong!", ex);
        }

        responseObserver.onNext(transactionResBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void receiveInfos(Api.InfoReq request, StreamObserver<Api.InfoRes> responseObserver) {

        StreamObserver<Api.InfoRes> observer = infoObservers.get(request.getId());
        if (observer != null) {
            observer.onCompleted();
            infoObservers.remove(request.getId());
        }

        infoObservers.put(request.getId(), responseObserver);
    }

    /**
     * MAKE THIS IN SERVICE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! To slow this way!
     *
     * @param infoRes
     */
    public void sentInfo(Api.InfoRes infoRes) {
        for (Map.Entry<Integer, StreamObserver<Api.InfoRes>> entry : infoObservers.entrySet()) {
            try {
                entry.getValue().onNext(infoRes);
            } catch (Exception ex) {
                entry.getValue().onError(ex);
                infoObservers.remove(entry.getKey());
            }
        }
    }

    public void publishPaymentRequest(PaymentRequest paymentRequest) throws Exception {

        Message.PaymentReq.Builder paymentRequestBuilder = Message.PaymentReq.newBuilder();

        ByteString msgId = ByteString.copyFrom(UUIDHelper.asBytes());

        Message.PaymentReq.Body body = Message.PaymentReq.Body.newBuilder()
                .setPaymentRequest(paymentRequest)
                .setMsgId(msgId)
                .setNodeId(nodeManager.getNodeId())
                .build();
        paymentRequestBuilder.setBody(body);
        paymentRequestBuilder.setSignature(cryptoManager.signData(body.toByteString()));

        idHandler.putId(msgId, DateHelper.dateNow().getTime());

        InfoRes infoRes = InfoRes.newBuilder()
                .setAccountId(paymentRequest.getToAccountId())
                .setInfoType(InfoRes.InfoType.PAYMENT_REQUEST)
                .setPaymentRequest(paymentRequest).build();

        sentInfo(infoRes);

        messageService.newMessage(BaseProcessor.makeMessage(Any.pack(paymentRequestBuilder.build())));
    }

    public void publish(Common.Block block) throws Exception {

        Message.BlockPub.Builder blockPubBuilder = Message.BlockPub.newBuilder();

        ByteString msgId = ByteString.copyFrom(UUIDHelper.asBytes());

        Message.BlockPub.Body body = Message.BlockPub.Body.newBuilder()
                .setBlock(block)
                .setMsgId(msgId)
                .setNodeId(nodeManager.getNodeId())
                .build();
        blockPubBuilder.setBody(body);
        blockPubBuilder.setSignature(cryptoManager.signData(body.toByteString()));

        idHandler.putId(msgId, DateHelper.dateNow().getTime());

        blockService.newBlock(Pair.of(nodeManager.getNodeId(), block));
        messageService.newMessage(BaseProcessor.makeMessage(Any.pack(blockPubBuilder.build())));
    }

}
