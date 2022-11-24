package com.ivzh.aws.s3;

import java.io.*;

public class S3OperationsHandler {


    public void writeToS3(String path, InputStream inputStream) {
        File file = new File("test.mp3");
        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[8192];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
