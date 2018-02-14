package xyz.jetdrone.detour.impl;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import xyz.jetdrone.detour.HttpContext;
import xyz.jetdrone.detour.Router;
import xyz.jetdrone.detour.RouterOptions;

import java.util.IdentityHashMap;
import java.util.Map;

public class RouterImpl implements Router {

  private final RouterOptions opts;
  private final Map<HttpMethod, Node> trees;

  public RouterImpl(RouterOptions opts) {
    if (opts.getPrefix() != null && opts.getPrefix().charAt(0) != '/') {
      throw new IllegalArgumentException("prefix must begin with '/' in path");
    }

    this.opts = opts;
    this.trees = new IdentityHashMap<>();
  }

  @Override
  public Router on(HttpMethod method, String path, Handler<HttpContext> handle) {
    if (path.charAt(0) != '/') {
      throw new IllegalArgumentException("path must begin with '/' in path");
    }

    if (!trees.containsKey(method)) {
      trees.put(method, new Node());
    }

    if (opts.getPrefix() != null) {
      path = opts.getPrefix() + path;
    }

    trees.get(method).addRoute(path, handle);

    return this;
  }

  private Handler[] find(Context ctx) {
    final HttpMethod verb = (HttpMethod) ctx.getVerb();
    final Node tree = trees.get(verb);
    if (tree != null) {
      return tree.search(ctx);
    }

    return null;
  }

  @Override
  public void handle(HttpServerRequest req) {
    final HttpContext ctx = new HttpContextImpl(req);

    final Handler[] needle = find(ctx);

    if (needle == null) {
      final Handler<Context> handle405 = opts.getMethodNotAllowedHandler();

      if (handle405 != null) {
        for (HttpMethod key : trees.keySet()) {
          if (key == ctx.getVerb()) {
            continue;
          }

          final Node tree = trees.get(key);
          // in this case we lookup as we don't want
          // to reparse the params
          if (tree.lookup(ctx) != null) {
            ctx.getResponse().setStatusCode(405);
            handle405.handle(ctx);
            return;
          }
        }
      }
    } else {
      needle[0].handle(ctx);
    }
  }
}
