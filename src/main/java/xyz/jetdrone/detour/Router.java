package xyz.jetdrone.detour;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import xyz.jetdrone.detour.impl.RouterImpl;

import static io.vertx.core.http.HttpMethod.*;

public interface Router extends Handler<HttpServerRequest> {

  static Router create() {
    return create(null);
  }

  static Router create(RouterOptions config) {
    return new RouterImpl(config == null ? new RouterOptions() : config);
  }

  Router on(HttpMethod method, String path, Handler<HttpContext> handle);

  default Router get(String path, Handler<HttpContext> handle) {
    return on(GET, path, handle);
  }

  default Router put(String path, Handler<HttpContext> handle) {
    return on(PUT, path, handle);
  }

  default Router post(String path, Handler<HttpContext> handle) {
    return on(POST, path, handle);
  }

  default Router delete(String path, Handler<HttpContext> handle) {
    return on(DELETE, path, handle);
  }

  default Router head(String path, Handler<HttpContext> handle) {
    return on(HEAD, path, handle);
  }

  default Router patch(String path, Handler<HttpContext> handle) {
    return on(PATCH, path, handle);
  }

  default Router options(String path, Handler<HttpContext> handle) {
    return on(OPTIONS, path, handle);
  }

  default Router trace(String path, Handler<HttpContext> handle) {
    return on(TRACE, path, handle);
  }

  default Router connect(String path, Handler<HttpContext> handle) {
    return on(CONNECT, path, handle);
  }

  default Router all(String path, Handler<HttpContext> handle) {
    for (HttpMethod method : HttpMethod.values()) {
      on(method, path, handle);
    }

    return this;
  }
}
