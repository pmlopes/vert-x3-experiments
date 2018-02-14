package xyz.jetdrone.detour.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import xyz.jetdrone.detour.HttpContext;

public class HttpContextImpl implements HttpContext {

  private final HttpServerRequest req;
  private final HttpServerResponse res;

  private MultiMap params;

  public HttpContextImpl(HttpServerRequest request) {
    this.req = request;
    this.res = request.response();
  }

  public HttpServerRequest getRequest() {
    return req;
  }

  public HttpServerResponse getResponse() {
    return res;
  }

  @Override
  public HttpMethod getVerb() {
    return req.method();
  }

  @Override
  public String getPath() {
    return req.path();
  }

  @Override
  public Context<HttpMethod> addParam(String key, String value) {
    if (params == null) {
      params = MultiMap.caseInsensitiveMultiMap();
    }

    params.add(key, value);
    return this;
  }
}
