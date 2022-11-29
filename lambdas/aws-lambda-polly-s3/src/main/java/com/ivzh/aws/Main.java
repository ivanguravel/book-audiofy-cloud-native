package com.ivzh.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.ivzh.aws.polly.PollyHandler;
import com.ivzh.aws.s3.S3OperationsHandler;

public class Main {


    public static void main(String[] args) {
        PollyHandler pollyHandler = new PollyHandler(Region.getRegion(Regions.US_EAST_1));

        S3OperationsHandler s3 = new S3OperationsHandler("le-experiment", pollyHandler, "123");

        s3.handle();


    }
}
