package com.flick.node.services;

import org.flinkcoin.data.proto.common.Common;
import org.flinkcoin.data.proto.storage.UnclaimedInfoBlock;
import org.flinkcoin.helper.helpers.DateHelper;
import org.flinkcoin.helper.helpers.UUIDHelper;
import com.flick.node.caches.AccountCache;
import com.flick.node.configuration.ProcessorBase;
import com.flick.node.storage.Storage;
import com.google.protobuf.ByteString;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BlockVotingServiceBase extends ProcessorBase<ByteString> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockVotingServiceBase.class);

    protected final Storage storage;

    public BlockVotingServiceBase(Storage storage) {
        super(PublishProcessor.create());
        this.storage = storage;
    }

    protected UnclaimedInfoBlock prepareInfoBlock(ByteString accountId, UnclaimedInfoBlock.Action action, ByteString blockHash) {
        UnclaimedInfoBlock unclaimedInfoBlock = UnclaimedInfoBlock.newBuilder()
                .setAccountId(accountId)
                .setAction(action)
                .setBlockHash(blockHash)
                .build();

        return unclaimedInfoBlock;
    }

    protected void persistBlock(Common.FullBlock block, Transaction t) throws RocksDBException {

        if (block.getBlock().getBody().getBlockType() == Common.Block.BlockType.RECEIVE) {
            Long time = DateHelper.dateNow().getTime();
            storage.putClaimedBlock(t, block.getBlock().getBody().getReceiveBlockHash(), time);

            storage.putUnclaimedInfoBlock(t, ByteString.copyFrom(UUIDHelper.asBytes()),
                    prepareInfoBlock(block.getBlock().getBody().getAccountId(), UnclaimedInfoBlock.Action.DELETE, block.getBlock().getBody().getReceiveBlockHash()));
        } else if (block.getBlock().getBody().getBlockType() == Common.Block.BlockType.SEND) {
            storage.putUnclaimedInfoBlock(t, ByteString.copyFrom(UUIDHelper.asBytes()),
                    prepareInfoBlock(block.getBlock().getBody().getSendAccountId(), UnclaimedInfoBlock.Action.CREATE, block.getBlock().getBlockHash().getHash()));
        }

        storage.putBlock(t, block.getBlock().getBlockHash().getHash(), block);
        storage.putAccount(t, block.getBlock().getBody().getAccountId(), block.getBlock().getBlockHash().getHash());
    }

}
