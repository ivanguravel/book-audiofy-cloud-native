package com.ivzh.aws.util;

import com.amazonaws.services.s3.event.S3EventNotification;


public class JsonUtil {

    public static String readFileNameFromS3EventJson(String json) {
        S3EventNotification record = S3EventNotification.parseJson(json);

        return record.getRecords().get(0).getS3().getObject().getKey();
    }

    private JsonUtil() {}
}
