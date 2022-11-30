package com.ivzh.aws.s3;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.ivzh.aws.polly.PollyHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class S3OperationsHandler {

    private final S3Object s3object;
    private final PollyHandler pollyHandler;
    private final TransferManager transferManager;
    private final String bucket;


    public S3OperationsHandler(String bucket, PollyHandler pollyHandler, String file) {
//        BasicAWSCredentials awsCreds = new
//                BasicAWSCredentials("",
//                "");
//        AWSCredentialsProvider provider = new AWSStaticCredentialsProvider(awsCreds);

        this.pollyHandler = pollyHandler;
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        this.s3object = s3.getObject(bucket, file);
        this.transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3)
                .build();
        this.bucket = bucket;
    }

    public void handle() {
        String line;
        try (S3ObjectInputStream s3InputStream = s3object.getObjectContent();
             BufferedReader reader = new BufferedReader(new InputStreamReader(s3InputStream, StandardCharsets.UTF_8))) {

            while ((line = reader.readLine()) != null) {
                InputStream stream = pollyHandler.handle(line, OutputFormat.Mp3);
                writeToS3(stream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeToS3(InputStream inputStream) {
        String fileName = "test11.mp3";
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("file", fileName);
        transferManager.upload(bucket, "test12.mp3", inputStream, metadata);
    }
}
