package xyz.jetdrone.detour;

import io.vertx.core.AbstractVerticle;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() {

    final Router app = Router.create();

    app.on(HttpMethod.GET, "/", ctx -> {
      ctx.getResponse().end("HOME");
    });

    vertx.createHttpServer()
      .requestHandler(app)
      .listen(8080, res -> {
      if (res.failed()) {
        res.cause().printStackTrace();
      } else {
        System.out.println("Server listening at: http://localhost:8080/");
      }
    });
  }

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }
}
