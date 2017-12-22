package xyz.jetdrone.blockchain;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import xyz.jetdrone.blockchain.impl.BlockchainImpl;

/**
 * Create a Blockchain API that can be used both internally or over the eventbus
 */
@VertxGen
public interface Blockchain {
  /**
   * Factory method.
   */
  static Blockchain blockchain(Vertx vertx, String baseAddress) {
    return new BlockchainImpl(vertx.eventBus(), baseAddress);
  }

  /**
   * Start the P2P sync.
   *
   * @param handler - Asynchronous result callback handler.
   * @return fluent self
   */
  @Fluent
  Blockchain start(Handler<AsyncResult<Void>> handler);

  @Fluent
  default Blockchain start() {
    return  start(onStart -> {});
  }

  /**
   * Stop the P2P sync.
   *
   * @param handler - Asynchronous result callback handler.
   * @return fluent self
   */
  @Fluent
  Blockchain stop(Handler<AsyncResult<Void>> handler);

  @Fluent
  default Blockchain stop() {
    return stop(onStop -> {});
  }

  /**
   * Returns the current size of the blockchain.
   *
   * @return size of the chain.
   */
  int size();

  /**
   * Returns the read only Block at a given position.
   *
   * @param index the index to lookup.
   * @return a block or null if index out of bounds.
   */
  Block get(int index);

  /**
   * Returns the read only Block at the end of the chain.
   *
   * @return a non null block.
   */
  Block last();

  /**
   * Event handler that will be invoked when a new block is added to the chain.
   *
   * @param handler - Asynchronous result callback handler.
   * @return fluent self
   */
  @Fluent
  Blockchain blockHandler(Handler<Block> handler);

  /**
   * Event handler that will be invoked when the chain is replaced (conflict resolution).
   *
   * @param handler - Asynchronous result callback handler.
   * @return fluent self
   */
  @Fluent
  Blockchain replaceHandler(Handler<Void> handler);

  /**
   * Add a the given date to the chain.
   * The data will be wrapped in a block and the inserted to the chain.
   *
   * @param data the data to store.
   * @param handler - Asynchronous result callback handler.
   * @return fluent self
   */
  @Fluent
  Blockchain add(String data, Handler<AsyncResult<Block>> handler);
}
