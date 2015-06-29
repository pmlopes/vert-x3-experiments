package io.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.util.Runner;

public class CRUDVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    Runner.run(".", CRUDVerticle.class);
  }

  @Override
  public void start() {

    final Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    // Create a mongo client using all defaults (connect to localhost and default port) using the database name "demo".
    final MongoClient mongo = MongoClient.createShared(vertx, new JsonObject().put("db_name", "test"));

    router.mountSubRouter("/api", MongoCRUD.create(mongo, "states").toRouter(vertx));

    router.route().handler(StaticHandler.create());

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}
