package io.vertx.demo;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.demo.impl.MongoCRUDImpl;
import io.vertx.ext.mongo.MongoClient;

@VertxGen
public interface MongoCRUD extends CRUD {

  static CRUD create(MongoClient mongo, String collection) {
    return new MongoCRUDImpl(mongo, collection);
  }

  static CRUD create(MongoClient mongo, String collection, String key) {
    return new MongoCRUDImpl(mongo, collection, key);
  }
}
