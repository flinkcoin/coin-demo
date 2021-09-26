package com.flick.node.services;

import com.flick.data.proto.common.Common.Block;
import com.flick.data.proto.communication.Message.BlockConfirmPub;
import com.flick.helper.Pair;
import com.flick.helper.helpers.UUIDHelper;
import com.flick.node.caches.NodeCache;
import com.flick.node.communication.CommonProcessor;
import com.flick.node.configuration.ProcessorBase;
import com.flick.node.handlers.ValidationHandler;
import com.flick.node.managers.CryptoManager;
import com.flick.node.managers.NodeManager;
import com.flick.node.voting.BlockVoting;
import com.flick.node.voting.stock.BlockStock;
import com.google.inject.Singleton;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockService extends ProcessorBase<Pair<ByteString, Block>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockService.class);

    private final Provider<CommonProcessor> commonHandler;
    private final NodeManager nodeManager;
    private final CryptoManager cryptoManager;
    private final BlockVoting blockVoting;
    private final BlockStock blockStock;
    private final ValidationHandler blockHandler;

    @Inject
    public BlockService(Provider<CommonProcessor> commonHandler, NodeCache nodeCache, NodeManager nodeManager, CryptoManager cryptoManager, BlockVoting blockVoting,
            BlockStock blockStock, ValidationHandler blockHandler) {
        super(PublishProcessor.create());
        this.blockVoting = blockVoting;
        this.nodeManager = nodeManager;
        this.cryptoManager = cryptoManager;
        this.commonHandler = commonHandler;
        this.blockStock = blockStock;
        this.blockHandler = blockHandler;
        this.publishProcessor
                .onBackpressureBuffer(1000, () -> {
                }, BackpressureOverflowStrategy.DROP_LATEST)
                .observeOn(Schedulers.single())
                .subscribe(this);
    }

    public void newBlock(Pair<ByteString, Block> pair) {
        publishProcessor.onNext(pair);
    }

    @Override
    public void process(Pair<ByteString, Block> pair) {

        ByteString nodeId = pair.getFirst();
        Block block = pair.getSecond();

        if (!blockHandler.validateBlock(block)) {
            return;
        }

        try {
            blockStock.putBlock(block.getBlockHash().getHash(), block);

            BlockConfirmPub.Builder blockExistConfirmPub = BlockConfirmPub.newBuilder();

            BlockConfirmPub.Body.Builder bodyBuilder = BlockConfirmPub.Body.newBuilder();

            bodyBuilder.setBlockHash(block.getBlockHash().getHash());
            bodyBuilder.setMsgId(ByteString.copyFrom(UUIDHelper.asBytes()));
            bodyBuilder.setNodeId(nodeManager.getNodeId());

            BlockConfirmPub.Body body = bodyBuilder.build();

            blockExistConfirmPub.setBody(body);
            blockExistConfirmPub.setSignature(cryptoManager.signData(body.toByteString()));
            commonHandler.get().flood(Any.pack(blockExistConfirmPub.build()));

            blockVoting.newBlockVote(Pair.of(nodeId, block.getBlockHash().getHash()));

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            LOGGER.error("Error!", ex);
        }
    }

}
