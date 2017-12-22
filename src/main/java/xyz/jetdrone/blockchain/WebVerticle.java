package xyz.jetdrone.blockchain;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class WebVerticle extends AbstractVerticle {

  @Override
  public void start() {
    final Router app = Router.router(vertx);

    // Allow events for the designated addresses in/out of the event bus bridge
    BridgeOptions opts = new BridgeOptions()
      .addOutboundPermitted(new PermittedOptions().setAddress("xyz.jetdrone.blockchain"))
      .addInboundPermitted(new PermittedOptions().setAddress("xyz.jetdrone.blockchain"));

    // Create the event bus bridge and add it to the app.
    SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
    app.route("/eventbus/*").handler(ebHandler);

    // Serve the static resources
    app.route().handler(StaticHandler.create());

    vertx.createHttpServer().requestHandler(app::accept).listen(8080);
  }
}
