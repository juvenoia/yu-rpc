package com.yupi.yurpc.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer {

    @Override
    public void doStart(int port) {
        Vertx vertx = Vertx.vertx();
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        server.requestHandler(req -> {
            System.out.println("Request received: " + req.method() + "" + req.uri());
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Vert.x HTTP Server!");
        });

        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.out.println("Failed to start server: " + result.cause());
            }
        });
    }
}
