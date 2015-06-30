package io.vertx.ext.web.crud.impl;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@FunctionalInterface
public interface CRUDHandler<T> {
  void handle(JsonObject filter, Handler<AsyncResult<T>> asyncResult);
}
