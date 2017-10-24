package xyz.jetdrone.blockchain;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import xyz.jetdrone.blockchain.impl.BlockchainImpl;

import java.util.List;

@VertxGen
public interface Blockchain {

  String QUERY_LATEST = "xyz.jetdrone.blockchain.query.latest";
  String QUERY_ALL = "xyz.jetdrone.blockchain.query.all";
  String RESPONSE_BLOCKCHAIN = "xyz.jetdrone.blockchain.query.get";

  static Blockchain create(Vertx vertx) {
    return new BlockchainImpl(vertx);
  }

  @Fluent
  Blockchain start();

  @Fluent
  Blockchain stop();

  Block get(int index);

  int size();

  Block getLatestBlock();

  void mine(String seed);

  void replaceChain(List<Block> newBlocks);

  boolean addBlock(Block newBlock);

  void addBlockFromPeer(Block json);

  Block generateNextBlock(String blockData);
}
