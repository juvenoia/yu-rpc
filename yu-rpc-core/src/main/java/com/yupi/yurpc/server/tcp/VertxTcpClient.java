package com.yupi.yurpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;

public class VertxTcpClient {
    public void start() {
        Vertx vertx = Vertx.vertx();
        vertx.createNetClient()
                .connect(8888, "localhost", result -> {
                    if (result.succeeded()) {
                        System.out.println("Connected to TCP server");
                        NetSocket socket = result.result();
                        socket.write("Hello, Server!");
                        socket.handler(buffer -> {
                            System.out.println("Received response from server: " + buffer.toString());
                        });
                    } else {
                        System.err.println("Failed to connect to TCP server");
                    }
                });
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}

