package xyz.jetdrone.blockchain.impl;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import xyz.jetdrone.blockchain.Block;
import xyz.jetdrone.blockchain.Blockchain;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockchainImpl implements Blockchain {

  private final Logger log = LoggerFactory.getLogger(BlockchainImpl.class);

  private static final Charset UTF8 = Charset.forName("UTF8");

  private final Vertx vertx;
  private final EventBus eb;

  private final MessageDigest sha256;

  private final List<Block> blockchain = new ArrayList<>();
  private final int difficulty;

  public BlockchainImpl(Vertx vertx) {
    try {
      sha256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    this.vertx = vertx;
    eb = vertx.eventBus();
    blockchain.add(Block.GENESIS);
    difficulty = 4;
  }

  @Override
  public Blockchain start() {
    // start up (sync with existing nodes)
    eb.<JsonArray>send(QUERY_ALL, new JsonObject(), sync -> {
      // ignore initial sync errors
      if (sync.succeeded()) {
        handle(sync.result());
      }

      // start listening for updates
      eb.consumer(RESPONSE_BLOCKCHAIN, this::handle);
      // reply to queries
      eb.consumer(QUERY_LATEST, message -> {
        log.trace("⬇  Peer requested for latest block.");

        log.debug("⬆  Sending peer latest block");
        message.reply(new JsonArray().add(getLatestBlock().toJson()));
      });

      eb.consumer(QUERY_ALL, message -> {
        log.trace("⬇  Peer requested for blockchain.");

        log.debug("⬆  Sending peer entire blockchain");

        final JsonArray json = new JsonArray();
        for (Block b : blockchain) {
          json.add(b.toJson());
        }

        message.reply(json);
      });
    });

    return this;
  }

  @Override
  public Blockchain stop() {
    return this;
  }

  private void handle(Message<JsonArray> message) {
    final List<Block> receivedBlocks = message.body()
      .stream()
      .map(json -> new Block((JsonObject) json))
      .collect(Collectors.toList());

    // sort
    receivedBlocks.sort((b1, b2) -> (b1.getIndex() - b2.getIndex()));

    final Block latestBlockReceived = receivedBlocks.get(receivedBlocks.size() - 1);
    final Block latestBlockHeld = getLatestBlock();

    log.trace("⬇  Peer sent over " + (receivedBlocks.size() == 1 ? "single block" : "blockchain") + ".");

    if (latestBlockReceived.getIndex() <= latestBlockHeld.getIndex()) {
      log.debug("💤  Received latest block is not longer than current blockchain. Do nothing");
      return;
    }

    log.info("🐢  Blockchain possibly behind. Received latest block is " + latestBlockReceived.getIndex() + ". Current latest block is " + latestBlockHeld.getIndex() + ".");

    if (latestBlockHeld.getHash().equals(latestBlockReceived.getPreviousHash())) {
      log.info("👍  Previous hash received is equal to current hash. Append received block to blockchain.");
      addBlockFromPeer(latestBlockReceived);

      log.debug("⬆  Sending peer latest block");
      eb.publish(RESPONSE_BLOCKCHAIN, new JsonArray().add(getLatestBlock().toJson()));
    } else if (receivedBlocks.size() == 1) {
      log.info("🤔  Received previous hash different from current hash. Get entire blockchain from peer.");

      log.debug("⬆  Asking peer for entire blockchain");
      eb.publish(QUERY_ALL, new JsonObject());
    } else {
      log.info("⛓  Peer blockchain is longer than current blockchain.");
      replaceChain(receivedBlocks);

      log.debug("⬆  Sending peer latest block");
      eb.publish(RESPONSE_BLOCKCHAIN, new JsonArray().add(getLatestBlock().toJson()));
    }
  }

  @Override
  public Block get(int index) {
    return blockchain.get(index);
  }

  @Override
  public int size() {
    return blockchain.size();
  }

  @Override
  public Block getLatestBlock() {
    return blockchain.get(blockchain.size() - 1);
  }

  @Override
  public void mine(String seed) {
    final Block newBlock = generateNextBlock(seed);
    if (addBlock(newBlock)) {
      log.info("🎉  Congratulations! A new block was mined. 💎");
    }
    // announce the new block
    eb.publish(RESPONSE_BLOCKCHAIN, new JsonArray().add(getLatestBlock().toJson()));
  }

  @Override
  public void replaceChain(List<Block> newBlocks) {
    if (!isValidChain(newBlocks)) {
      log.info("❌ Replacement chain is not valid. Won't replace existing blockchain.");
      return;
    }

    if (newBlocks.size() <= blockchain.size()) {
      log.info("❌  Replacement chain is shorter than original. Won't replace existing blockchain.");
      return;
    }

    log.info("✅  Received blockchain is valid. Replacing current blockchain with received blockchain");
    blockchain.clear();

    blockchain.addAll(newBlocks);
  }

  private boolean isValidChain(List<Block> blockchainToValidate) {
    if (!Block.GENESIS.equals(blockchainToValidate.get(0))) {
      return false;
    }

    final List<Block> tempBlocks = new ArrayList<>();
    tempBlocks.add(blockchainToValidate.get(0));

    for (int i = 1; i < blockchainToValidate.size(); i++) {
      if (isValidNewBlock(blockchainToValidate.get(i), tempBlocks.get(i - 1))) {
        tempBlocks.add(blockchainToValidate.get(i));
      } else {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addBlock(Block newBlock) {
    if (isValidNewBlock(newBlock, getLatestBlock())) {
      blockchain.add(newBlock);
      return true;
    }
    return false;
  }

  @Override
  public void addBlockFromPeer(Block json) {
    if (isValidNewBlock(json, getLatestBlock())) {
      blockchain.add(
        new Block()
          .setIndex(json.getIndex())
          .setPreviousHash(json.getPreviousHash())
          .setTimestamp(json.getTimestamp())
          .setData(json.getData())
          .setHash(json.getHash())
          .setNonce(json.getNonce()));
    }
  }

  private String calculateHashForBlock(Block block) {
    return calculateHash(block.getIndex(), block.getPreviousHash(), block.getTimestamp(), block.getData(), block.getNonce());
  }

  private synchronized String calculateHash(int index, String previousHash, long timestamp, String data, int nonce) {
    sha256.update((index + previousHash + timestamp + data + nonce).getBytes(UTF8));
    return bytesToHex(sha256.digest());
  }

  private boolean isValidNewBlock(Block newBlock, Block previousBlock) {
    final String blockHash = calculateHashForBlock(newBlock);

    if (previousBlock.getIndex() + 1 != newBlock.getIndex()) {
      log.info("❌  new block has invalid index");
      return false;
    } else if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
      log.info("❌  new block has invalid previous hash");
      return false;
    } else if (!blockHash.equals(newBlock.getHash())) {
      log.info("❌  invalid hash:" + blockHash + " " + newBlock.getHash());
      return false;
    } else if (!isValidHashDifficulty(calculateHashForBlock(newBlock))) {
      log.info("❌  invalid hash does not meet difficulty requirements: " + calculateHashForBlock(newBlock));
      return false;
    }
    return true;
  }

  @Override
  public Block generateNextBlock(String blockData) {
    final Block previousBlock = getLatestBlock();
    final int nextIndex = previousBlock.getIndex() + 1;
    final long nextTimestamp = System.currentTimeMillis() / 1000;
    int nonce = 0;
    String nextHash = "";

    while (!isValidHashDifficulty(nextHash)) {
      nonce = nonce + 1;
      nextHash = calculateHash(nextIndex, previousBlock.getHash(), nextTimestamp, blockData, nonce);
    }

    return new Block()
      .setIndex(nextIndex)
      .setPreviousHash(previousBlock.getHash())
      .setTimestamp(nextTimestamp)
      .setData(blockData)
      .setHash(nextHash)
      .setNonce(nonce);
  }

  private boolean isValidHashDifficulty(String hash) {
    int i, b;
    for (i = 0, b = hash.length(); i < b; i++) {
      if (hash.charAt(i) != '0') {
        break;
      }
    }
    return i == difficulty;
  }

  private final static char[] HEX = "0123456789abcdef".toCharArray();

  private static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX[v >>> 4];
      hexChars[j * 2 + 1] = HEX[v & 0x0F];
    }
    return new String(hexChars);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[\n");
    for (Block block : blockchain) {
      sb.append("  ");
      sb.append(block.toJson().encode());
      sb.append("\n");
    }
    sb.append("]\n");

    return sb.toString();
  }
}
