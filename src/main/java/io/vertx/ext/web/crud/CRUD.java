package io.vertx.ext.web.crud;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
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
  CRUD validatorHandler(Handler<RoutingContext> handler);

  Router toRouter(final Vertx vertx);
}
