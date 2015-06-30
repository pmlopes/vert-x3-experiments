package io.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.crud.JDBCCRUD;
import io.vertx.ext.web.crud.MongoCRUD;
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

//    // Create a mongo client using all defaults (connect to localhost and default port) using the database name "demo".
//    final MongoClient mongo = MongoClient.createShared(vertx, new JsonObject().put("db_name", "test"));

    // Create a JDBC client with a test database
    final JDBCClient jdbc = JDBCClient.createShared(vertx, new JsonObject()
        .put("url", "jdbc:postgresql:postgres")
        .put("user", "postgres")
        .put("password", "mysecretpassword")
        .put("driver_class", "org.postgresql.Driver"));


//    router.mountSubRouter("/api", MongoCRUD.create(mongo, "states").toRouter(vertx));
    router.mountSubRouter("/api", JDBCCRUD.create(jdbc, "films").toRouter(vertx));

    router.route().handler(StaticHandler.create());

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}
