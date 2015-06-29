package io.vertx.demo;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

@VertxGen(concrete = false)
public interface CRUD {

  @Fluent
  CRUD enableCreate(boolean state);

  @Fluent
  CRUD enableRead(boolean state);

  @Fluent
  CRUD enableUpdate(boolean state);

  @Fluent
  CRUD enableDelete(boolean state);

  @Fluent
  CRUD createHandler(CRUDHandler<JsonObject> handler);

  @Fluent
  CRUD readHandler(CRUDHandler<List<JsonObject>> handler);

  @Fluent
  CRUD updateHandler(CRUDHandler<Void> handler);

  @Fluent
  CRUD deleteHandler(CRUDHandler<Void> handler);

  @Fluent
  CRUD countHandler(CRUDHandler<Long> handler);

  @Fluent
  CRUD validatorHandler(Handler<RoutingContext> handler);

  Router toRouter(final Vertx vertx);
}
