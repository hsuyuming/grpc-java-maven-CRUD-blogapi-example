package com.github.abehsu.blog.client;

import com.proto.blog.*;
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

//        createBlog(channel);
//        readBlog(channel,"5e32d41d4312142195a47576");
//        readBlog(channel,"5e32d41d531214");
        updateBlog(channel,"5e32d41d4312142195a47576");
        System.out.println("shutdown channel");
        channel.shutdown();


    }

    private void updateBlog(ManagedChannel channel, String blogId) {

        BlogServiceGrpc.BlogServiceBlockingStub stubClient = BlogServiceGrpc.newBlockingStub(channel);

        UpdateBlogRequest updateBlogRequest = UpdateBlogRequest.newBuilder()
                .setBlog(
                        Blog.newBuilder()
                                .setId(blogId)
                                .setAuthorId("abesss")
                                .setTitle("second title")
                                .setContent("hello~~~~~")
                                .build()
                )
                .build();
        try {
            UpdateBlogResponse updateBlogResponse = stubClient.updateBlog(updateBlogRequest);
            System.out.println(updateBlogResponse.toString());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    private void readBlog(ManagedChannel channel, String blogId) {

        BlogServiceGrpc.BlogServiceBlockingStub stubClient = BlogServiceGrpc.newBlockingStub(channel);

        try {

            ReadBlogResponse readBlogResponse =  stubClient.readBlog(ReadBlogRequest
                    .newBuilder()
                    .setBlogId(blogId)
                    .build());

            System.out.printf(readBlogResponse.toString());
        } catch (RuntimeException e) {
            System.out.println("Reading blog with non existing id.....");
            e.printStackTrace();
        }

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
