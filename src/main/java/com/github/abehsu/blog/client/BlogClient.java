package com.github.abehsu.blog.client;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import javax.net.ssl.SSLException;
import java.io.File;

public class BlogClient {

    public void run() throws SSLException {
        System.out.println("Create Channel");
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhsot",50052)
                .sslContext(
                        GrpcSslContexts.forClient().trustManager(
                                new File("ssl/ca.crt")
                        ).build()
                ).build();

        createBlog(channel);


        System.out.println("shutdown channel");
        channel.shutdown();


    }

    private void createBlog(ManagedChannel channel) {
        
    }


    public static void main(String[] args) throws SSLException {
        BlogClient main = new BlogClient();
        main.run();
    }
}
