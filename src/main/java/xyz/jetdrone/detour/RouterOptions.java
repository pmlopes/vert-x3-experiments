package xyz.jetdrone.detour;

import io.vertx.core.Handler;
import xyz.jetdrone.detour.impl.Context;

public class RouterOptions {

  private String prefix;
  private Handler<Context> methodNotAllowedHandler;

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public Handler<Context> getMethodNotAllowedHandler() {
    return methodNotAllowedHandler;
  }

  public void setMethodNotAllowedHandler(Handler<Context> methodNotAllowedHandler) {
    this.methodNotAllowedHandler = methodNotAllowedHandler;
  }
}
