package com.ivzh.aws;

import com.amazonaws.HttpMethod;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivzh.aws.util.PropertiesLoader;

import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class S3UrlGenerationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    private static final String FILE_PARAM = "file";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AmazonS3 s3Client;
    private final String bucket;
    private final long linkExpirationTime;

    public S3UrlGenerationHandler() {
        PropertiesLoader loader = new PropertiesLoader();
        this.s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        this.bucket = loader.getBucket();
        this.linkExpirationTime = loader.getLinkExpirationTime();
    }


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event,
                                                      Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("generate url for getting s3 object");

        Map<String, String> pathParameters = event.getPathParameters();
        String file = pathParameters.getOrDefault(FILE_PARAM, "");
        String url = generatePreSignedUrl(file);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setBody(safeGetS3UrlData(url));
        logger.log(response.toString());
        return response;
    }

    private String generatePreSignedUrl(String object) {

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, object)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(createExpirationDate());
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    private Date createExpirationDate() {
        Date expiration = new Date();
        long expTimeMillis = Instant.now().toEpochMilli();
        expTimeMillis += linkExpirationTime;
        expiration.setTime(expTimeMillis);
        return expiration;
    }

    private static String safeGetS3UrlData(String url) {
        try {
            return OBJECT_MAPPER.writeValueAsString(Collections.singletonMap("url", url));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
