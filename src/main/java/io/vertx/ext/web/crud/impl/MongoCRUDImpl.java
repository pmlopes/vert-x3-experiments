package io.vertx.ext.web.crud.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.crud.CRUD;

public class MongoCRUDImpl extends CRUDImpl {

  private final MongoClient mongo;

  public MongoCRUDImpl(MongoClient mongo, String collection) {
    super(collection, "_id");
    this.mongo = mongo;
    enableCreate(true);
    enableRead(true);
    enableUpdate(true);
    enableDelete(true);
  }

  public MongoCRUDImpl(MongoClient mongo, String collection, String key) {
    super(collection, key);
    this.mongo = mongo;
    enableCreate(true);
    enableRead(true);
    enableUpdate(true);
    enableDelete(true);
  }

  @Override
  public CRUD enableCreate(boolean state) {
    if (state) {
      create = (filter, res) -> {
        mongo.insert(collection, filter.getJsonObject("value"), res1 -> {
          if (res1.failed()) {
            res.handle(Future.failedFuture(res1.cause()));
            return;
          }

          final String genId = res1.result();

          JsonObject result = filter.getJsonObject("value");

          if (genId != null) {
            result = result.copy();
            result.put(key, genId);
          }

          res.handle(Future.succeededFuture(result));
        });
      };
    } else {
      create = null;
    }
    return this;
  }

  @Override
  public CRUD enableRead(boolean state) {
    if (state) {
      read = (filter, res) -> {
        JsonObject sort = filter.getJsonObject("sort");
        Integer start = filter.getInteger("start");
        Integer end = filter.getInteger("end");

        FindOptions options = new FindOptions();

        if (sort != null) {
          options.setSort(sort);
        }

        if (start != null) {
          options.setSkip(start);
        }

        if (end != null) {
          options.setLimit(end);
        }

        mongo.findWithOptions(collection, filter.getJsonObject("query"), options, res);
      };

      count = (filter, res) -> mongo.count(collection, filter.getJsonObject("query"), res);
    } else {
      read = null;
      count = null;
    }

    return this;
  }

  @Override
  public CRUD enableUpdate(boolean state) {
    if (state) {
      update = (filter, res) -> mongo.replace(collection, filter.getJsonObject("query"), filter.getJsonObject("value"), res);
    } else {
      update = null;
    }
    return this;
  }

  @Override
  public CRUD enableDelete(boolean state) {
    if (state) {
      delete = (filter, res) -> mongo.removeOne(collection, filter.getJsonObject("query"), res);
    } else {
      delete = null;
    }
    return this;
  }
}
