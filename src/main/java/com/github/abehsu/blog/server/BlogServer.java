package com.github.abehsu.blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;

public class BlogServer {
    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Start gRPC server.....");

        Server server = ServerBuilder.forPort(50052)
                .addService(new BlogServiceImpl())
                .useTransportSecurity(
                        new File("ssl/server.crt"),
                        new File("ssl/server.pem")
                )
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            System.out.println("Prepare to shutdown server....");
            server.shutdown();
            System.out.println("Successfully shutdown server");
        } ) );


        server.awaitTermination();


    }
}
