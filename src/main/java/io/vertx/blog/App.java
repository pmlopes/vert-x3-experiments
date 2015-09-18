package io.vertx.blog;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.util.Runner;

public class App extends AbstractVerticle {

  public static void main(String[] args) {
    Runner.run(App.class);
  }

  @Override
  public void start() {
    Router router = Router.router(vertx);

    router.get("/hello").handler(rc -> {
      rc.response()
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("greeting", "Hello World!").encode());
    });

    // optionally enable the web console so users can play with your API
    // online from their web browsers
    router.route().handler(StaticHandler.create());

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}
