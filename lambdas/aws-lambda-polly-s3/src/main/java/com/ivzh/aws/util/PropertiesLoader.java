package com.ivzh.aws.util;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private static final String BUCKET = "bucket";
    private static final String REGION = "region";

    private final Properties properties;

    {
         properties = new Properties();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classloader.getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public String getBucket() {
        return properties.getProperty(BUCKET);
    }

    public String getRegion() {
        return properties.getProperty(REGION);
    }
}
