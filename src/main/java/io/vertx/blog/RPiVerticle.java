package io.vertx.blog;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class RPiVerticle extends AbstractVerticle {

  private static final OperatingSystemMXBean osMBean;

  static {
    try {
      osMBean = ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(),
          ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void start() {

    Router router = Router.router(vertx);

    router.route("/eventbus/*").handler(SockJSHandler.create(vertx)
        .bridge(new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddress("load"))));

    router.route().handler(StaticHandler.create());

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);

    vertx.setPeriodic(1000, t -> vertx.eventBus().publish("load",
        new JsonObject()
            .put("creatTime", System.currentTimeMillis())
            .put("cpuTime", osMBean.getSystemLoadAverage())));
  }
}