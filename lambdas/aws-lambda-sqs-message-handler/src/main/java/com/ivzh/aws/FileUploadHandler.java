package com.ivzh.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.MultipartStream;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileUploadHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event,
                                                      Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of Proxy");

        //Log the length of the incoming body
        logger.log(String.valueOf(event.getBody().getBytes().length));

        //Create the APIGatewayProxyResponseEvent response
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        //Set up contentType String
        String contentType = "";

        //Change these values to fit your region and bucket name
        String clientRegion = "us-east-1";
        String bucketName = "le-experiment";

        //Every file will be named image.jpg in this example.
        //You will want to do something different here in production
        String fileObjKeyName = "photo_2021-05-20_19-52-18.jpg";

        try {

            //Get the uploaded file and decode from base64
            byte[] bI = Base64.decodeBase64(event.getBody().getBytes());

            //Get the content-type header and extract the boundary
            Map<String, String> hps = event.getHeaders();
            if (hps != null) {
                contentType = hps.get("content-type");
            }
            String[] boundaryArray = contentType.split("=");

            //Transform the boundary to a byte array
            byte[] boundary = boundaryArray[1].getBytes();

            //Log the extraction for verification purposes
            logger.log(new String(bI, "UTF-8") + "\n");

            //Create a ByteArrayInputStream
            ByteArrayInputStream content = new ByteArrayInputStream(bI);

            //Create a MultipartStream to process the form-data
            MultipartStream multipartStream =
                    new MultipartStream(content, boundary, bI.length, null);

            //Create a ByteArrayOutputStream
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            //Find first boundary in the MultipartStream
            boolean nextPart = multipartStream.skipPreamble();

            //Loop through each segment
            while (nextPart) {
                String header = multipartStream.readHeaders();

                //Log header for debugging
                logger.log("Headers:");
                logger.log(header);

                //Write out the file to our ByteArrayOutputStream
                multipartStream.readBodyData(out);
                //Get the next part, if any
                nextPart = multipartStream.readBoundary();
            }

            //Log completion of MultipartStream processing
            logger.log("Data written to ByteStream");

            //Prepare an InputStream from the ByteArrayOutputStream
            InputStream fis = new ByteArrayInputStream(out.toByteArray());

            //Create our S3Client Object
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();

            //Configure the file metadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(out.toByteArray().length);
            metadata.setContentType("image/jpeg");
            metadata.setCacheControl("public, max-age=31536000");

            //Put file into S3
            s3Client.putObject(bucketName, fileObjKeyName, fis, metadata);

            //Log status
            logger.log("Put object in S3");

            //Provide a response
            response.setStatusCode(200);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("Status", "File stored in S3");
            String responseBodyString = OBJECT_MAPPER.writeValueAsString(responseBody);
            response.setBody(responseBodyString);

        }
         catch (Exception e) {
            e.printStackTrace();
        }

        logger.log(response.toString());
        return response;
    }
}
