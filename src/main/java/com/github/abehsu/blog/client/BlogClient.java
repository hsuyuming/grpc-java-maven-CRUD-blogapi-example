package com.github.abehsu.blog.client;

import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import javax.net.ssl.SSLException;
import java.io.File;

public class BlogClient {

    public void run() throws SSLException {
        System.out.println("Create Channel");
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhost",50052)
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

        BlogServiceGrpc.BlogServiceBlockingStub stubClient = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setAuthorId("abehsu")
                .setTitle("New Blog")
                .setContent("Hello world, this is my first blog!")
                .build();

        CreateBlogResponse response =  stubClient.createBlog(CreateBlogRequest.newBuilder()
                .setBlog(blog)
                .build());

        System.out.println("Received create blog response");
        System.out.println(response.toString());


    }


    public static void main(String[] args) throws SSLException {
        BlogClient main = new BlogClient();
        main.run();
    }
}
