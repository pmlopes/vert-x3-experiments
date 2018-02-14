package xyz.jetdrone.detour;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import xyz.jetdrone.detour.impl.Context;

public interface HttpContext extends Context<HttpMethod> {

  HttpServerRequest getRequest();
  HttpServerResponse getResponse();
}
