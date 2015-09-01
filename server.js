var Router = require("vertx-web-js/router");
var SockJSHandler = require("vertx-web-js/sock_js_handler");
var StaticHandler = require("vertx-web-js/static_handler");

var router = Router.router(vertx);

// Allow outbound traffic to the draw address

var options = {
    "outboundPermitteds" : [
        {
            "address" : "draw"
        }
    ],
    "inboundPermitteds" : [
        {
            "address" : "draw"
        }
    ]
};

router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options).handle);

// Serve the static resources
router.route().handler(StaticHandler.create().handle);

vertx.createHttpServer().requestHandler(router.accept).listen(8080);