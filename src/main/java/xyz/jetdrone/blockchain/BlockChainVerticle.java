package xyz.jetdrone.blockchain;

import io.vertx.core.AbstractVerticle;

public class BlockChainVerticle extends AbstractVerticle {

  private Blockchain blockchain;

  @Override
  public void start() {
    blockchain = Blockchain
      .blockchain(vertx, "xyz.jetdrone.blockchain")
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
            if (cmd.length < 2) {
              System.out.println("not enough arguments: m payload");
              break;
            }
            vertx.runOnContext(v -> {
              blockchain.add(cmd[1], onAdd -> {
                if (onAdd.failed()) {
                  onAdd.cause().printStackTrace();
                }
              });
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
