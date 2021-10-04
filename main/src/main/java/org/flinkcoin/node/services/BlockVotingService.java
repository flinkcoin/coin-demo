package org.flinkcoin.node.services;

import org.flinkcoin.data.proto.api.Api;
import org.flinkcoin.data.proto.common.Common;
import org.flinkcoin.data.proto.common.Common.FullBlock;
import org.flinkcoin.data.proto.storage.UnclaimedInfoBlock;
import org.flinkcoin.helper.ThrowableConsumer;
import org.flinkcoin.helper.helpers.UUIDHelper;
import org.flinkcoin.node.api.AccountServiceImpl;
import org.flinkcoin.node.caches.AccountCache;
import org.flinkcoin.node.storage.Storage;
import org.flinkcoin.node.voting.stock.BlockStock;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockVotingService extends BlockVotingServiceBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockVotingService.class);

    private final BlockStock blockStock;
    private final Provider<AccountServiceImpl> accountServiceImpl;
    private final AccountCache accountCache;

    @Inject
    public BlockVotingService(Storage storage, BlockStock blockStock, Provider<AccountServiceImpl> accountServiceImpl, AccountCache accountCache) {
        super(storage);
        this.accountServiceImpl = accountServiceImpl;
        this.accountCache = accountCache;
        this.blockStock = blockStock;
        this.publishProcessor
                .onBackpressureBuffer(1000, () -> {
                }, BackpressureOverflowStrategy.DROP_LATEST)
                .observeOn(Schedulers.single())
                .subscribe(this);
    }

    public void newBlock(ByteString blockHash) {
        publishProcessor.onNext(blockHash);
    }

    @Override
    public void process(ByteString blockHash) {
        Optional<Common.Block> block = blockStock.getBlock(blockHash);

        if (block.isEmpty()) {
            LOGGER.debug("Something not ok here, block missing!");
        }

        try {
            storage.newTransaction(new ThrowableConsumer<Transaction>() {
                @Override
                public void acceptThrowsException(Transaction t) throws Exception {
                    Optional<FullBlock> fullBlock = storage.getBlock(t, block.get().getBody().getPreviousBlockHash());

                    FullBlock.Builder fullBlockBuilder;
                    if (fullBlock.isPresent()) {
                        fullBlockBuilder = fullBlock.get().toBuilder();
                        fullBlockBuilder.setNext(block.get().getBlockHash().getHash());
                        storage.putBlock(t, fullBlock.get().getBlock().getBlockHash().getHash(), fullBlockBuilder.build());
                    }

                    fullBlockBuilder = FullBlock.newBuilder();
                    fullBlockBuilder.setBlock(block.get());

                    persistBlock(fullBlockBuilder.build(), t);
                }
            });

            accountCache.setLastBlockHash(block.get().getBody().getAccountId(), block.get().getBlockHash().getHash());

            blockStock.remove(blockHash);
        } catch (RocksDBException ex) {
            LOGGER.error("Could not write vote result to DB", ex);
        }

        Api.InfoRes infoRes = Api.InfoRes.newBuilder()
                .setInfoType(Api.InfoRes.InfoType.BLOCK_CONFIRM)
                .setBlockConfirm(Api.InfoRes.BlockConfirm.newBuilder().setBlockHash(blockHash))
                .setAccountId(block.get().getBody().getAccountId())
                .build();
        accountServiceImpl.get().sentInfo(infoRes);

        if (block.get().getBody().getBlockType() == Common.Block.BlockType.SEND) {

            infoRes = Api.InfoRes.newBuilder()
                    .setInfoType(Api.InfoRes.InfoType.PAYMENT_RECEIVED)
                    .setPaymentReceived(Api.InfoRes.PaymentReceived.newBuilder().setBlockHash(blockHash))
                    .setAccountId(block.get().getBody().getSendAccountId())
                    .build();
            accountServiceImpl.get().sentInfo(infoRes);
        }
    }

}
