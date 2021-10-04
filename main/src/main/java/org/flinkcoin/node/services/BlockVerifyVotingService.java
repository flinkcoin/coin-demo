package org.flinkcoin.node.services;

import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.List;
import javax.inject.Inject;
import org.flinkcoin.data.proto.common.Common;
import org.flinkcoin.data.proto.common.Common.FullBlock;
import org.flinkcoin.node.caches.AccountCache;
import org.flinkcoin.node.storage.Storage;
import org.flinkcoin.node.voting.stock.BlockVerifyStock;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockVerifyVotingService extends BlockVotingServiceBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockVerifyVotingService.class);

    private final BlockVerifyStock blockVerifyStock;
    private final Storage storage;
    private final AccountCache accountCache;

    @Inject
    public BlockVerifyVotingService(BlockVerifyStock blockVerifyStock, Storage storage, AccountCache accountCache) {
        super(storage);
        this.accountCache = accountCache;
        this.blockVerifyStock = blockVerifyStock;
        this.storage = storage;
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
        List<FullBlock> blocks = blockVerifyStock.getBlock(blockHash);

        if (blocks.isEmpty()) {
            LOGGER.debug("Something not ok here, block missing!");
            return;
        }

        try {
            storage.newTransaction(t -> {
                for (FullBlock fb : blocks) {
                    persistBlock(fb, t);
                }
            });

            for (FullBlock fb : blocks) {
                Common.Block.Body body = fb.getBlock().getBody();
                Common.Block block = fb.getBlock();
                accountCache.setLastBlockHash(body.getAccountId(), block.getBlockHash().getHash());
            }

            blockVerifyStock.remove(blockHash);
        } catch (RocksDBException ex) {
            LOGGER.error("Could not write vote result to DB", ex);
        }

    }

}
