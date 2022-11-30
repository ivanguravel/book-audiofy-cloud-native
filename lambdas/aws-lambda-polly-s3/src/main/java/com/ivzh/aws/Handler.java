package com.ivzh.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.ivzh.aws.polly.PollyHandler;
import com.ivzh.aws.s3.S3OperationsHandler;
import com.ivzh.aws.util.JsonUtil;
import com.ivzh.aws.util.PropertiesLoader;

public class Handler implements RequestHandler<SQSEvent, Void> {
    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        System.out.println("data from event: ");
        System.out.println(event.getRecords().get(0));

        String fileName = JsonUtil.readFileNameFromS3EventJson(String.valueOf(event.getRecords().get(0)));
        System.out.println(fileName);

        PropertiesLoader loader = new PropertiesLoader();

        PollyHandler pollyHandler = new PollyHandler(Region.getRegion(Regions.valueOf(loader.getRegion())));

        S3OperationsHandler s3 = new S3OperationsHandler(loader.getBucket(), pollyHandler, "123");

        s3.handle();
        return null;
    }
}