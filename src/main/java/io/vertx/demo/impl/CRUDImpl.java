package io.vertx.demo.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.demo.CRUDHandler;
import io.vertx.demo.CRUD;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CRUDImpl implements CRUD {

  private static final Pattern RANGE = Pattern.compile("items=(\\d+)-(\\d+)");
  private static final Pattern SORT = Pattern.compile("sort\\((.+)\\)");


  protected final String collection;
  protected final String key;

  private String sortParam;

  private Handler<RoutingContext> validator;

  protected CRUDHandler<JsonObject> create;
  protected CRUDHandler<List<JsonObject>> read;
  protected CRUDHandler<Void> update;
  protected CRUDHandler<Void> delete;
  protected CRUDHandler<Long> count;

  public CRUDImpl(String collection, String key) {
    this.collection = collection;
    this.key = key;
  }

  public CRUDImpl sortParam(String sortParam) {
    this.sortParam = sortParam;
    return this;
  }

  public CRUDImpl validatorHandler(Handler<RoutingContext> handler) {
    this.validator = handler;
    return this;
  }

  public Router toRouter(final Vertx vertx) {

    final Router router = Router.router(vertx);
    if (validator != null) {
      router.route("/" + collection).handler(validator);
    }
    // CREATE
    router.post("/" + collection).handler(this::create);
    // READ ONE
    router.get("/" + collection + "/:" + collection + "Id").handler(this::read);
    // READ ALL
    router.get("/" + collection).handler(this::query);
    // UPDATE
    router.put("/" + collection + "/:" + collection + "Id").handler(this::update);
    // DELETE
    router.delete("/" + collection + "/:" + collection + "Id").handler(this::delete);
    // shortcut for patch (as by Dojo Toolkit)
    router.post("/" + collection + "/:" + collection + "Id").handler(this::append);
    router.patch("/" + collection + "/:" + collection + "Id").handler(this::append);

    return router;
  }

  @Override
  public CRUDImpl createHandler(CRUDHandler<JsonObject> handler) {
    this.create = handler;
    return this;
  }

  @Override
  public CRUDImpl readHandler(CRUDHandler<List<JsonObject>> handler) {
    this.read = handler;
    return this;
  }

  @Override
  public CRUDImpl updateHandler(CRUDHandler<Void> handler) {
    this.update = handler;
    return this;
  }

  @Override
  public CRUDImpl deleteHandler(CRUDHandler<Void> handler) {
    this.delete = handler;
    return this;
  }

  @Override
  public CRUDImpl countHandler(CRUDHandler<Long> handler) {
    this.count = handler;
    return this;
  }

  private void delete(final RoutingContext ctx) {
    if (delete == null) {
      ctx.fail(405);
      return;
    }

    // get the real id from the params multimap
    final String id = ctx.request().params().get(collection + "Id");

    final JsonObject filter = new JsonObject()
        .put("query", new JsonObject().put(key, id));

    final JsonObject userFilter = ctx.get("filter");

    if (userFilter != null) {
      filter.mergeIn(userFilter);
    }

    delete.handle(filter, res -> {
      if (res.failed()) {
        ctx.fail(res.cause());
        return;
      }

      if (!res.succeeded()) {
        ctx.fail(404);
      } else {
        ctx.request().response().setStatusCode(204);
        ctx.request().response().end();
      }
    });
  }

  private void create(final RoutingContext ctx) {
    if (create == null) {
      ctx.fail(405);
      return;
    }

    final JsonObject item = ctx.getBodyAsJson();

    if (item == null) {
      // body must be json
      ctx.fail(400);
      return;
    }

    final JsonObject filter = new JsonObject()
        .put("value", item);

    final JsonObject userFilter = ctx.get("filter");

    if (userFilter != null) {
      filter.mergeIn(userFilter);
    }

    create.handle(filter, res -> {
      if (res.failed()) {
        ctx.fail(res.cause());
        return;
      }

      final JsonObject result = res.result();

      ctx.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; utf-8")
          .putHeader(HttpHeaders.LOCATION, ctx.request().absoluteURI() + result.getValue(key))
          .setStatusCode(201)
          .end(result.encode());
    });
  }

  private void update(final RoutingContext ctx) {
    if (update == null) {
      ctx.fail(405);
      return;
    }

    final JsonObject item = ctx.getBodyAsJson();

    if (item == null) {
      // body must be json
      ctx.fail(400);
      return;
    }

    // get the real id from the params multimap
    String id = ctx.request().params().get(collection + "Id");

    final JsonObject filter = new JsonObject()
        .put("value", item)
        .put("query", new JsonObject().put(key, id));

    final JsonObject userFilter = ctx.get("filter");

    if (userFilter != null) {
      filter.mergeIn(userFilter);
    }

    update.handle(filter, res -> {
      if (res.failed()) {
        ctx.fail(res.cause());
        return;
      }

      if (!res.succeeded()) {
        ctx.fail(404);
      } else {
        ctx.response()
            .setStatusCode(204)
            .end();
      }
    });
  }

  private void read(final RoutingContext ctx) {
    if (read == null) {
      ctx.fail(405);
      return;
    }

//    // TODO: content negotiation
//    if (request.accepts("application/json") == null) {
//      // Not Acceptable (we only talk json)
//      next.handle(406);
//      return;
//    }

    // get the real id from the params multimap
    final String id = ctx.request().params().get(collection + "Id");

    final JsonObject filter = new JsonObject()
        .put("query", new JsonObject().put(key, id));

    final JsonObject userFilter = ctx.get("filter");

    if (userFilter != null) {
      filter.mergeIn(userFilter);
    }

    read.handle(filter, res -> {
      if (res.failed()) {
        ctx.fail(res.cause());
        return;
      }

      final List<JsonObject> items = res.result();

      if (items == null || items.size() == 0) {
        ctx.fail(404);
        return;
      }

      final JsonObject item = items.get(0);

      if (item == null) {
        ctx.fail(404);
      } else {
        ctx.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; utf-8")
            .end(item.encode());
      }
    });
  }

  private void append(final RoutingContext ctx) {
    if (read == null || update == null) {
      ctx.fail(405);
      return;
    }

    // get the real id from the params multimap
    final String id = ctx.request().params().get(collection + "Id");

    final JsonObject filter = new JsonObject()
        .put("query", new JsonObject().put(key, id));

    final JsonObject userFilter = ctx.get("filter");

    if (userFilter != null) {
      filter.mergeIn(userFilter);
    }

    read.handle(filter, res -> {
      if (res.failed()) {
        ctx.fail(res.cause());
        return;
      }

      final String ifMatch = ctx.request().getHeader("If-Match");
      final String ifNoneMatch = ctx.request().getHeader("If-None-Match");

      // merge existing json with incoming one
      final boolean overwrite =
          // pure PUT, must exist and will be updated
          (ifMatch == null && ifNoneMatch == null) ||
              // must exist and will be updated
              ("*".equals(ifMatch));

      final List<JsonObject> items = res.result();

      if (items == null || items.size() == 0) {
        ctx.fail(404);
        return;
      }

      final JsonObject item = items.get(0);

      if (item == null) {
        // does not exist but was marked as overwrite
        if (overwrite) {
          // does not exist, returns 412
          ctx.fail(412);
        } else {
          // does not exist, returns 404
          ctx.fail(404);
        }
      } else {
        // does exist but was marked as not overwrite
        if (!overwrite) {
          // does exist, returns 412
          ctx.fail(412);
        } else {
          item.mergeIn(ctx.getBodyAsJson());

          final JsonObject filter1 = new JsonObject()
              .put("value", item)
              .put("query", new JsonObject().put(key, id));

          final JsonObject userFilter1 = ctx.get("filter");

          if (userFilter1 != null) {
            filter1.mergeIn(userFilter1);
          }

          // update back to the db
          update.handle(filter1, res1 -> {
            if (res1.failed()) {
              ctx.fail(res1.cause());
              return;
            }

            if (!res1.succeeded()) {
              ctx.fail(404);
            } else {
              ctx.response()
                  .setStatusCode(204)
                  .end();
            }
          });
        }
      }
    });
  }

  private static Integer parseInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException nfe) {
      return null;
    }
  }

  private void query(final RoutingContext ctx) {
    if (read == null) {
      ctx.fail(405);
      return;
    }

//    // TODO: content negotiation
//    if (request.accepts("application/json") == null) {
//      // Not Acceptable (we only talk json)
//      next.handle(406);
//      return;
//    }

    // parse ranges
    final String range = ctx.request().getHeader("range");

    final Integer start, end;

    if (range != null) {
      Matcher m = RANGE.matcher(range);
      if (m.matches()) {
        start = parseInt(m.group(1));
        end = parseInt(m.group(2));
      } else {
        start = null;
        end = null;
      }
    } else {
      start = null;
      end = null;
    }

    // parse query
    final JsonObject dbquery = new JsonObject();
    final JsonObject dbsort = new JsonObject();

    for (Map.Entry<String, String> entry : ctx.request().params()) {
      String[] sortArgs;
      // parse sort
      if (sortParam == null) {
        Matcher sort = SORT.matcher(entry.getKey());

        if (sort.matches()) {
          sortArgs = sort.group(1).split(",");
          for (String arg : sortArgs) {
            if (arg.charAt(0) == '+' || arg.charAt(0) == ' ') {
              dbsort.put(arg.substring(1), 1);
            } else if (arg.charAt(0) == '-') {
              dbsort.put(arg.substring(1), -1);
            }
          }
          continue;
        }
      } else {
        if (sortParam.equals(entry.getKey())) {
          sortArgs = entry.getValue().split(",");
          for (String arg : sortArgs) {
            if (arg.charAt(0) == '+' || arg.charAt(0) == ' ') {
              dbsort.put(arg.substring(1), 1);
            } else if (arg.charAt(0) == '-') {
              dbsort.put(arg.substring(1), -1);
            }
          }
          continue;
        }
      }
      dbquery.put(entry.getKey(), entry.getValue());
    }

    final JsonObject filter = new JsonObject()
        .put("query", dbquery)
        .put("sort", dbsort)
        .put("start", start)
        .put("end", end);

    final JsonObject userFilter = ctx.get("filter");

    if (userFilter != null) {
      filter.mergeIn(userFilter);
    }

    read.handle(filter, res -> {
      if (res.failed()) {
        ctx.fail(res.cause());
        return;
      }

      final List<JsonObject> result = res.result();

      if (result == null) {
        ctx.fail(404);
      } else {
        if (range != null && count != null) {
          // need to send the content-range with totals
          count.handle(filter, res1 -> {
            if (res1.failed()) {
              ctx.fail(res1.cause());
              return;
            }

            Long count = res1.result();

            if (count != null) {
              Integer realEnd = end;

              if (start != null && end != null) {
                realEnd = start + result.size();
              }

              ctx.response()
                  .putHeader(HttpHeaders.CONTENT_RANGE, "items " + start + "-" + realEnd + "/" + count)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; utf-8")
                  .end(new JsonArray(result).encode());

            }
          });
        } else {
          ctx.response()
              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; utf-8")
              .end(new JsonArray(result).encode());
        }
      }
    });
  }
}
