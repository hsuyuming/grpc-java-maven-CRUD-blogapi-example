package com.github.abehsu.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {

        System.out.println("Received Create Blog request");

        Blog blog = request.getBlog();

        Document doc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        // we insert(create) the document in MongoDB
        System.out.println("Inserting blog//////");
        collection.insertOne(doc);

        // We retrive the MongoDB generated ID
        String id = doc.getObjectId("_id").toString();
        System.out.println("Inserted blog: " + id);

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(
                        blog.toBuilder()
                            .setId(id)
                            .build()
                )
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();

    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {


        System.out.println("Received Read Blog request");
        String blogId = request.getBlogId();

        System.out.println("Searching for a blog");

        Document result = null;

        try {
            result =collection.find(eq("_id",new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
//            return;

        }

        if (result == null ) {
            // we don't have a match
            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                    .withDescription("The blog with the corresponding id was not found")
                    .asRuntimeException()
            );
            return;
        } else {
            System.out.println("Blog found, sending the response");
            Blog blog = Blog.newBuilder()
                    .setAuthorId(result.getString("author_id"))
                    .setTitle(result.getString("title"))
                    .setContent(result.getString("content"))
                    .setId(blogId)
                    .build();

            responseObserver.onNext(ReadBlogResponse.newBuilder()
                    .setBlog(blog)
                    .build());

            responseObserver.onCompleted();
        }

    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {

        System.out.println("Received Update Blog request");

        Blog blog = request.getBlog();

        String blogId = blog.getId();

        Document result = null;

        System.out.println("Searching for a blog so we can update it");
        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                    .withDescription(e.getLocalizedMessage())
                    .asRuntimeException()
            );
            return;
        }

        if ( result == null ) {
            System.out.println(" Blog not find ");
            responseObserver.onError(
                    Status.NOT_FOUND
                    .withDescription("The blog with the corresponding id was not found")
                    .asRuntimeException()
            );
            return;
        } else {

            Document replacement = new Document("author_id", blog.getAuthorId())
                    .append("title", blog.getTitle())
                    .append("content", blog.getContent())
                    .append("_id",new ObjectId(blogId));

            System.out.println("Replaced! Sending as a response");
            collection.replaceOne(eq("_id", result.getObjectId("_id")), replacement);

            responseObserver.onNext(
                    UpdateBlogResponse.newBuilder()
                            .setBlog(documentToBlog(replacement))
                            .build()
            );

            responseObserver.onCompleted();

        }



    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {

        System.out.println("Received Delete Blog Response");
        String blogId = request.getBlogId().toString();

        DeleteResult result = null;

        System.out.println("Start to delete id: " + blogId);
        try {
            result = collection.deleteOne(eq("_id", new ObjectId(blogId)));
        } catch (Exception e ) {
            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                    .withDescription(e.getLocalizedMessage())
                    .asRuntimeException()
            );
            return;
        }

        if ( result.getDeletedCount() == 0 ) {
            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException()
            );
        } else {
            System.out.println("Blog was deleted");
            responseObserver.onNext(DeleteBlogResponse.newBuilder()
                    .setBlogId(blogId)
                    .build());
            responseObserver.onCompleted();
        }


    }

    @Override
    public void listBlog(ListBlogRequest request, StreamObserver<ListBlogResponse> responseObserver) {
        System.out.println("Received List Blog Request");

        collection.find().iterator().forEachRemaining(document -> {
            responseObserver.onNext(ListBlogResponse.newBuilder()
                    .setBlog(documentToBlog(document))
                    .build()
            );
        });

        responseObserver.onCompleted();
    }

    private Blog documentToBlog(Document document) {
        return Blog.newBuilder()
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .setId(document.getObjectId("_id").toString())
                .build();

    }
}
