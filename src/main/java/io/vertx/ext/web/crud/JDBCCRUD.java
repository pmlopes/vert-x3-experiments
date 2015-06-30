package io.vertx.ext.web.crud;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.crud.impl.JDBCCRUDImpl;

@VertxGen
public interface JDBCCRUD extends CRUD {

  static CRUD create(JDBCClient client, String table) {
    return new JDBCCRUDImpl(client, table);
  }

  static CRUD create(JDBCClient client, String table, String key) {
    return new JDBCCRUDImpl(client, table, key);
  }
}
