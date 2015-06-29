package io.vertx.demo;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@FunctionalInterface
public interface CRUDHandler<T> {
  void handle(JsonObject filter, Handler<AsyncResult<T>> asyncResult);
}
