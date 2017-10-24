package xyz.jetdrone.blockchain;

import io.vertx.core.AbstractVerticle;

public class BlockChainVerticle extends AbstractVerticle {

  private Blockchain blockchain;

  @Override
  public void start() {
    blockchain = Blockchain
      .create(vertx)
      .start();

    final Thread shell = new Thread(() -> {
      while (true) {
        System.out.print(":> ");
        String line = System.console().readLine();

        final String[] cmd = line.split(" ");

        switch (cmd[0]) {
          case "bc":
            vertx.runOnContext(v -> {
              System.out.println(blockchain);
            });
            break;
          case "m":
            vertx.runOnContext(v -> {
              blockchain.mine(cmd[1]);
            });
            break;
        }
      }

    }, "blockchain-shell");

    shell.setDaemon(true);
    shell.start();
  }

  @Override
  public void stop() {
    blockchain.stop();
  }
}
