package io.vertx.ext.web.crud.impl;

import io.vertx.core.Future;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.crud.CRUD;

public class JDBCCRUDImpl extends CRUDImpl {

  private final JDBCClient db;

  public JDBCCRUDImpl(JDBCClient db, String collection) {
    super(collection, "id");
    this.db = db;
    enableCreate(true);
    enableRead(true);
    enableUpdate(true);
    enableDelete(true);
  }

  public JDBCCRUDImpl(JDBCClient db, String collection, String key) {
    super(collection, key);
    this.db = db;
    enableCreate(true);
    enableRead(true);
    enableUpdate(true);
    enableDelete(true);
  }

  @Override
  public CRUD enableCreate(boolean state) {
    if (state) {
      create = (filter, res) -> {
        res.handle(Future.failedFuture("Not Implemented!"));
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
        res.handle(Future.failedFuture("Not Implemented!"));
      };
    } else {
      read = null;
    }
    return this;
  }

  @Override
  public CRUD enableUpdate(boolean state) {
    if (state) {
      update = (filter, res) -> {
        res.handle(Future.failedFuture("Not Implemented!"));
      };
    } else {
      update = null;
    }
    return this;
  }

  @Override
  public CRUD enableDelete(boolean state) {
    if (state) {
      delete = (filter, res) -> {
        res.handle(Future.failedFuture("Not Implemented!"));
      };
    } else {
      delete = null;
    }
    return this;
  }
}
