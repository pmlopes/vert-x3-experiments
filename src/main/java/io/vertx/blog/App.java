package io.vertx.blog;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
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
          .end(new JsonObject().encode());
    });

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}
