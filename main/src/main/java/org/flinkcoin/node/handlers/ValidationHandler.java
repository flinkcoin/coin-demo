package org.flinkcoin.node.handlers;

import org.flinkcoin.crypto.CryptoException;
import org.flinkcoin.crypto.HashHelper;
import org.flinkcoin.data.proto.common.Common;
import org.flinkcoin.data.proto.common.Common.FullBlock;
import org.flinkcoin.helper.helpers.Base32Helper;
import org.flinkcoin.node.caches.AccountCache;
import org.flinkcoin.node.caches.BlockCache;
import org.flinkcoin.node.caches.ClaimedBlockCache;
import org.flinkcoin.node.managers.CryptoManager;
import org.flinkcoin.node.managers.NodeManager;
import com.google.protobuf.ByteString;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ValidationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationHandler.class);

    private final NodeManager nodeManager;
    private final CryptoManager cryptoManager;
    private final BlockCache blockCache;
    private final AccountCache accountCache;
    private final ClaimedBlockCache claimedBlockCache;

    @Inject
    public ValidationHandler(NodeManager nodeManager, CryptoManager cryptoManager, BlockCache blockCache, AccountCache accountCache, ClaimedBlockCache claimedBlockCache) {
        this.nodeManager = nodeManager;
        this.cryptoManager = cryptoManager;
        this.blockCache = blockCache;
        this.accountCache = accountCache;
        this.claimedBlockCache = claimedBlockCache;
    }

    public boolean validateBlock(Common.Block block) {

        Common.Block.BlockType blockType = block.getBody().getBlockType();

        boolean success = true;

        try {
            checkHash(block);
            FullBlock previousBlock;
            switch (blockType) {
                case CREATE:
                    checkAccountNotExist(block);
                    break;
                case SEND:
                    previousBlock = checkAccountAndPreviousBlock(block);
                    checkSignature(previousBlock, block);
                    checkSendBlock(previousBlock, block);

                    break;
                case RECEIVE:
                    previousBlock = checkAccountAndPreviousBlock(block);
                    checkSignature(previousBlock, block);
                    checkReceiveBlock(previousBlock, block);

                    break;
                case UPDATE:
                    previousBlock = checkAccountAndPreviousBlock(block);
                    checkSignature(previousBlock, block);
                    checkUpdateBlock(previousBlock, block);

                    break;
                default:
                    throw new ValidationException("Strange block type!");
            }

        } catch (Exception ex) {
            success = false;
            LOGGER.warn("Block problem!", ex);    
        }

        return success;
    }

    private void checkSendBlock(FullBlock previousBlock, Common.Block block) throws ValidationException, CryptoException {
        if (block.getBody().getBalance() < 0) {
            throw new ValidationException("Zero balance not possible, not a Bank!");
        }

        long amount = previousBlock.getBlock().getBody().getBalance() - block.getBody().getBalance();

        if (!Objects.equals(amount, block.getBody().getAmount())) {
            throw new ValidationException("Amount not ok!");
        }
    }

    private void checkReceiveBlock(FullBlock previousBlock, Common.Block receiveBlock) throws ValidationException, CryptoException {

        Optional<Long> claimedBlockTime = claimedBlockCache.getClaimedBlockTime(receiveBlock.getBody().getReceiveBlockHash());

        if (claimedBlockTime.isPresent()) {
            throw new ValidationException(MessageFormat.format("Receive block {1} already claimed at {0}!", claimedBlockTime.get(), Base32Helper.encode(receiveBlock.getBody().getReceiveBlockHash().toByteArray()) ));
        }

        Optional<FullBlock> sendBlock = blockCache.getBlock(receiveBlock.getBody().getReceiveBlockHash());

        if (sendBlock.isEmpty()) {
            throw new ValidationException("No send block to receive from!");
        }

        if (sendBlock.get().getBlock().getBody().getBlockType() != Common.Block.BlockType.SEND) {
            throw new ValidationException(MessageFormat.format("Trying to receive from not send block {0}!",
                    Base32Helper.encode(sendBlock.get().getBlock().getBlockHash().getHash().toByteArray()))
            );
        }

        if (!Objects.equals(sendBlock.get().getBlock().getBody().getSendAccountId(), receiveBlock.getBody().getAccountId())) {
            throw new ValidationException(
                    MessageFormat.format("Trying to receive from not correct account id {0}!",
                            Base32Helper.encode(sendBlock.get().getBlock().getBody().getSendAccountId().toByteArray())
                    )
            );
        }

        if (!Objects.equals(sendBlock.get().getBlock().getBody().getAmount(), receiveBlock.getBody().getAmount())) {
            throw new ValidationException("Amount not ok!");
        }

        long amount = receiveBlock.getBody().getBalance() - previousBlock.getBlock().getBody().getBalance();

        if (!Objects.equals(sendBlock.get().getBlock().getBody().getAmount(), amount)) {
            throw new ValidationException("Amount not ok!");
        }
    }

    private void checkUpdateBlock(FullBlock previousBlock, Common.Block block) throws ValidationException, CryptoException {

        if (!Objects.equals(previousBlock.getBlock().getBody().getAmount(), block.getBody().getAmount())) {
            throw new ValidationException("Amount has to be the same!");
        }

        if (!Objects.equals(previousBlock.getBlock().getBody().getBalance(), block.getBody().getBalance())) {
            throw new ValidationException("balance has to be the same!");
        }
    }

    private void checkHash(Common.Block block) throws ValidationException, CryptoException {
        Common.Block.Body body = block.getBody();

        byte[] hash = HashHelper.sha512(body.toByteArray());

        if (!Arrays.equals(hash, block.getBlockHash().getHash().toByteArray())) {
            throw new ValidationException("Block hash not ok!");
        }
    }

    private void checkSignature(FullBlock previousBlock, Common.Block block) throws ValidationException, CryptoException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Common.Block.Body body = block.getBody();

        Common.Block.PublicKeys publicKeys = previousBlock.getBlock().getBody().getPublicKeys();

        Common.Block.SignatureMode singatureMode = publicKeys.getSingatureMode();

        int numberOfSignaturesOk = 0;

        for (int i = 0; i < publicKeys.getPublicKeyCount(); i++) {
            ByteString publicKey = publicKeys.getPublicKey(i);
            if (cryptoManager.verifyBlockData(publicKey, body.toByteString(), block.getSignatues().getSignature(i))) {
                numberOfSignaturesOk++;
            }
        }

        switch (singatureMode) {
            case ONE_OF_ONE:
                if (numberOfSignaturesOk < 1) {
                    throw new ValidationException("Block signature not ok!");
                }
                break;
            case TWO_OF_THREE:
                if (numberOfSignaturesOk < 2) {
                    throw new ValidationException("Block signature not ok!");
                }
                break;
            case THREE_OF_THREE:
                if (numberOfSignaturesOk < 3) {
                    throw new ValidationException("Block signature not ok!");
                }
                break;
            case THREE_OF_FIVE:
                if (numberOfSignaturesOk < 3) {
                    throw new ValidationException("Block signature not ok!");
                }
                break;
            case FIVE_OF_FIVE:
                if (numberOfSignaturesOk < 5) {
                    throw new ValidationException("Block signature not ok!");
                }
                break;
        }

    }

    private void checkAccountNotExist(Common.Block block) throws ValidationException {
        Common.Block.Body body = block.getBody();
        ByteString accountId = body.getAccountId();
        Optional<ByteString> blockHash = accountCache.getLastBlockHash(accountId);
        if (blockHash.isPresent()) {
            throw new ValidationException(MessageFormat.format("Account {0} already exist!", Base32Helper.encode(accountId.toByteArray())));
        }
    }

    private Common.FullBlock checkAccountAndPreviousBlock(Common.Block block) throws ValidationException {
        Common.Block.Body body = block.getBody();
        ByteString accountId = body.getAccountId();
        Optional<ByteString> blockHash = accountCache.getLastBlockHash(accountId);
        if (blockHash.isEmpty()) {
            throw new ValidationException(MessageFormat.format("Account {0} does not exist!", Base32Helper.encode(accountId.toByteArray())));
        }

        Optional<Common.FullBlock> previousBlock = blockCache.getBlock(body.getPreviousBlockHash());

        if (previousBlock.isEmpty()) {
            String msg = MessageFormat.format("Previous block {0} does not exist for account {1}!", Base32Helper.encode(body.getPreviousBlockHash().toByteArray()),
                    Base32Helper.encode(accountId.toByteArray()));
            throw new ValidationException(msg);
        }

        if (!blockHash.get().equals(body.getPreviousBlockHash())) {
            String msg = MessageFormat.format("Previous block {0} does not match {1} - {2}!", Base32Helper.encode(body.getPreviousBlockHash().toByteArray()),
                    Base32Helper.encode(accountId.toByteArray()),
                    Base32Helper.encode(blockHash.get().toByteArray())
            );
            throw new ValidationException(msg);
        }

        if (!Objects.equals(previousBlock.get().getBlock().getBody().getAccountId(), block.getBody().getAccountId())) {
            String msg = MessageFormat.format("Previous block {0} has different accountId {1} - {2}!", Base32Helper.encode(body.getPreviousBlockHash().toByteArray()),
                    Base32Helper.encode(previousBlock.get().getBlock().getBody().getAccountId().toByteArray()),
                    Base32Helper.encode(block.getBody().getAccountId().toByteArray())
            );
            throw new ValidationException(msg);
        }

        return previousBlock.get();
    }

    public static class ValidationException extends Exception {

        static final long serialVersionUID = 1L;

        public ValidationException(String errorMessage) {
            super(errorMessage);
        }
    }

}
