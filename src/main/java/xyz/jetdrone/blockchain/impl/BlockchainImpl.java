package xyz.jetdrone.blockchain.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import xyz.jetdrone.blockchain.Block;
import xyz.jetdrone.blockchain.Blockchain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static xyz.jetdrone.blockchain.impl.Hash.sha256;

public class BlockchainImpl implements Blockchain {

  private final EventBus eb;
  private final String baseAddress;
  private final List<Block> chain;
  // mutable state
  private MessageConsumer messageConsumer;
  private Handler<Block> blockHandler;
  private Handler<Void> replaceHandler;

  public BlockchainImpl(EventBus eb, String baseAddress) {
    this.eb = eb;
    this.baseAddress = baseAddress;
    // the internal state
    chain = new ArrayList<>();
    // insert the genesis block
    chain.add(new Block()
      .setIndex(0)
      .setNonce(1)
      .setPreviousHash("")
      .setData("<genesis>"));
  }

  @Override
  public final Blockchain start(Handler<AsyncResult<Void>> handler) {
    eb.send(baseAddress, null, onSend -> {
      if (onSend.succeeded()) {
        consensus(((JsonArray) onSend.result().body())
          .stream()
          .map(json -> new Block((JsonObject) json))
          .collect(Collectors.toList()));
      }
    });

    // start listening for events
    messageConsumer = eb.consumer(baseAddress, message -> {
      if (message.body() != null) {
        // if there is a body it is a mine event
        consensus(Collections.singletonList(new Block((JsonObject) message.body())));
      } else {
        // when there is no message it's a chain request
        JsonArray json = new JsonArray();
        for (Block block : chain) {
          json.add(block.toJson());
        }

        message.reply(json);
      }
    });

    if (handler != null) {
      handler.handle(Future.succeededFuture());
    }

    return this;
  }

  @Override
  public Blockchain stop(Handler<AsyncResult<Void>> handler) {
    if (messageConsumer != null) {
      messageConsumer.unregister(handler);
      messageConsumer = null;
    }

    return this;
  }

  @Override
  public final int size() {
    return chain.size();
  }

  @Override
  public final Block get(int index) {
    if (index < 0 || index >= chain.size()) {
      return null;
    }

    return chain.get(index);
  }

  @Override
  public final Block last() {
    return chain.get(chain.size() - 1);
  }

  @Override
  public final Blockchain blockHandler(Handler<Block> handler) {
    this.blockHandler = handler;
    return this;
  }

  @Override
  public final Blockchain replaceHandler(Handler<Void> handler) {
    this.replaceHandler = handler;
    return this;
  }

  @Override
  public final Blockchain add(String data, Handler<AsyncResult<Block>> handler) {
    // we run the proof of work algorithm to get the next proof...
    Block lastBlock = last();
    int lastProof = lastBlock.getNonce();
    int proof = proofOfWork(lastProof);

    // forge the new Block by adding it to the chain
    final Block block = new Block()
      .setIndex(chain.size())
      .setTimestamp(System.currentTimeMillis())
      .setData(data)
      .setNonce(proof)
      .setPreviousHash(hash(lastBlock));

    chain.add(block);
    // notify the event handler
    if (blockHandler != null) {
      blockHandler.handle(block);
    }
    // announce to peers the new mined block
    eb.publish(baseAddress, block.toJson());

    handler.handle(Future.succeededFuture(block));
    return this;
  }

  /**
   * Creates a SHA-256 hash of a Block
   *
   * @param block the block to encode
   * @return sha256 hex value
   */
  private static String hash(Block block) {
    return sha256(block.getIndex() + ":" + block.getPreviousHash() + ":" + block.getTimestamp() + ":" + block.getData() + ":" + block.getNonce());
  }

  /**
   * Simple Proof of Work Algorithm:
   * - Find a number p' such that hash(pp') contains leading 4 zeroes, where p is the previous p'
   * - p is the previous proof, and p' is the new proof
   *
   * @param lastProof the previous proof
   * @return the current proof
   */
  public int proofOfWork(int lastProof) {
    int proof = 0;
    while (!validProof(lastProof, proof)) {
      proof++;
    }

    return proof;
  }

  /**
   * Validates the Proof. (Implementation detail)
   *
   * @param lastProof Previous proof
   * @param proof     Current proof
   * @return true if correct, false if not
   */
  private static boolean validProof(int lastProof, int proof) {
    String guess = Integer.toString(lastProof) + Integer.toString(proof);
    String guessHash = sha256(guess);
    // hardcoded difficulty of 4
    return guessHash.substring(0, 4).equals("0000");
  }

  /**
   * Determine if a given blockchain is valid.
   *
   * @param chain A blockchain
   * @return True if valid, False if not
   */
  public final boolean validChain(List<Block> chain) {
    Block lastBlock = chain.get(0);
    int currentIndex = 1;

    while (currentIndex < chain.size()) {
      Block block = chain.get(currentIndex);
      // check that the hash of the block is correct
      if (!block.getPreviousHash().equals(hash(lastBlock))) {
        return false;
      }
      // check that the Proof of Work is correct
      if (!validProof(lastBlock.getNonce(), block.getNonce())) {
        return false;
      }

      lastBlock = block;
      currentIndex++;
    }

    return true;
  }

  /**
   * This is our consensus algorithm, it resolves conflicts
   * by replacing our chain with the longest one in the network.
   */
  public void consensus(List<Block> receivedBlocks) {
    final Block latestBlockReceived = receivedBlocks.get(receivedBlocks.size() - 1);
    final Block latestBlockHeld = last();

    if (latestBlockReceived.getIndex() <= latestBlockHeld.getIndex()) {
      // received latest block is not longer than current store. Do nothing
      return;
    }

    if (hash(latestBlockHeld).equals(latestBlockReceived.getPreviousHash())) {
      // previous hash received is equal to current hash. Append received block to store.
      chain.add(latestBlockReceived);
      // notify the event handler
      if (blockHandler != null) {
        blockHandler.handle(latestBlockReceived);
      }
    } else if (receivedBlocks.size() == 1) {
      // received previous hash different from current hash. Get entire store from peer.
      eb.send(baseAddress, null, onSend -> {
        if (onSend.succeeded()) {
          consensus(((JsonArray) onSend.result().body())
            .stream()
            .map(json -> new Block((JsonObject) json))
            .collect(Collectors.toList()));
        }
      });
    } else {
      // Peer store is longer than current store.
      if (validChain(receivedBlocks)) {
        chain.clear();
        chain.addAll(receivedBlocks);
        // notify the event handler
        if (replaceHandler != null) {
          replaceHandler.handle(null);
        }
      }
    }
  }

  @Override
  public String toString() {
    return Json.encodePrettily(chain);
  }
}
