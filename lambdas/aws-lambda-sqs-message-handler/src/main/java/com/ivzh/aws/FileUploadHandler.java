package com.ivzh.aws;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.MultipartStream;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FileUploadHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String BUCKET_NAME = "le-experiment";


    private final AmazonS3 s3Client;

    public FileUploadHandler() {
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
    }


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event,
                                                      Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of Proxy");

        logger.log(String.valueOf(event.getBody().getBytes().length));

        String contentType = "";
        try {

            byte[] fileBytes = Base64.decodeBase64(event.getBody().getBytes());

            Map<String, String> headers = event.getHeaders();
            if (headers != null) {
                contentType = headers.get("content-type");
            }
            String[] boundaryArray = contentType.split("=");

            byte[] boundary = boundaryArray[1].getBytes();

            logger.log(new String(fileBytes, "UTF-8") + "\n");

            ByteArrayInputStream content = new ByteArrayInputStream(fileBytes);

            MultipartStream multipartStream =
                    new MultipartStream(content, boundary, fileBytes.length, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            boolean nextPart = multipartStream.skipPreamble();

            while (nextPart) {
                String header = multipartStream.readHeaders();

                logger.log("Headers:");
                logger.log(header);

                multipartStream.readBodyData(out);
                nextPart = multipartStream.readBoundary();
            }

            //Log completion of MultipartStream processing
            logger.log("Data written to ByteStream");

            //Prepare an InputStream from the ByteArrayOutputStream
            InputStream fis = new ByteArrayInputStream(out.toByteArray());

            s3Client.putObject(BUCKET_NAME,
                    event.getHeaders().getOrDefault("file", ""),
                    fis, createObjectMetadata(out.toByteArray().length, contentType));

            //Log status
            logger.log("object has been added to S3");
        }
         catch (Exception e) {
            e.printStackTrace();
        }

        return createResponse(Collections.singletonMap("Status", "File stored in S3"));
    }


    private static ObjectMetadata createObjectMetadata(int fileSize, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileSize);
        metadata.setContentType(contentType);
        metadata.setCacheControl("public, max-age=31536000");
        return metadata;
    }


    private static APIGatewayProxyResponseEvent createResponse(Map<String, String> responseBody) {
        try {
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(200);
            String responseBodyString = OBJECT_MAPPER.writeValueAsString(responseBody);
            response.setBody(responseBodyString);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
