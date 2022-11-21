package org.flinkcoin.node.services;

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

import org.flinkcoin.data.proto.common.Common.FullBlock;
import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.data.proto.communication.Message.BlockConfirmPub;
import org.flinkcoin.helper.helpers.UUIDHelper;
import org.flinkcoin.node.caches.BlockCache;
import org.flinkcoin.node.caches.NodeCache;
import org.flinkcoin.node.communication.CommonProcessor;
import org.flinkcoin.node.configuration.ProcessorBase;
import org.flinkcoin.node.managers.CryptoManager;
import org.flinkcoin.node.managers.NodeManager;
import org.flinkcoin.node.voting.stock.BlockVerifyStock;
import com.google.inject.Singleton;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockVerifyService extends ProcessorBase<ByteString> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockVerifyService.class);

    private final Provider<CommonProcessor> commonHandler;
    private final NodeManager nodeManager;
    private final CryptoManager cryptoManager;
    private final BlockCache blockCache;
    private final BlockVerifyStock blockVerifyStock;

    @Inject
    public BlockVerifyService(Provider<CommonProcessor> commonHandler, NodeCache nodeCache, NodeManager nodeManager, CryptoManager cryptoManager, BlockCache blochCache,
            BlockVerifyStock blockVerifyStock) {
        super(PublishProcessor.create());
        this.blockCache = blochCache;
        this.nodeManager = nodeManager;
        this.cryptoManager = cryptoManager;
        this.commonHandler = commonHandler;
        this.blockVerifyStock = blockVerifyStock;
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

        if (!validateBlock(blockHash)) {
            return;
        }

        Optional<FullBlock> block = blockCache.getBlock(blockHash);

        if (block.isEmpty()) {
            return;
        }

        try {

            BlockConfirmPub.Builder blockExistConfirmPub = Message.BlockConfirmPub.newBuilder();

            BlockConfirmPub.Body.Builder bodyBuilder = BlockConfirmPub.Body.newBuilder();

            bodyBuilder.setBlockHash(block.get().getBlock().getBlockHash().getHash());
            bodyBuilder.setMsgId(ByteString.copyFrom(UUIDHelper.asBytes()));
            bodyBuilder.setNodeId(nodeManager.getNodeId());

            BlockConfirmPub.Body body = bodyBuilder.build();

            blockExistConfirmPub.setBody(body);
            blockExistConfirmPub.setSignature(cryptoManager.signData(body.toByteString()));
            commonHandler.get().flood(Any.pack(blockExistConfirmPub.build()));

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            LOGGER.error("Error!", ex);
        }

    }

    public boolean validateBlock(ByteString blockHash) {

        return true;

    }

}
