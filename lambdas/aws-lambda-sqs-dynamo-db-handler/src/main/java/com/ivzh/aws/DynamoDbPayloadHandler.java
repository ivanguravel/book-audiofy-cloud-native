package com.ivzh.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class DynamoDbPayloadHandler implements RequestHandler<SQSEvent, Void> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DynamoDB dynamoDB;

    public DynamoDbPayloadHandler() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        this.dynamoDB = new DynamoDB(client);
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        Map<String, String> map = new HashMap<>();

        for (SQSMessage msg : event.getRecords()) {
            map.putAll(msg.getAttributes());
        }
        String fileName = JsonUtil.readFileNameFromS3EventJson(String.valueOf(event.getRecords().get(0)));
        Table table = dynamoDB.getTable("books-metadata");
        table.putItem(createItem(map, fileName));
        return null;
    }

    private Item createItem(Map<String, String> parameters, String fileName) {
        return new Item()
                .withJSON("BookMetadata", safeConvertObjectToJson(parameters))
                .withPrimaryKey("BookId", fileName);
    }

    private String safeConvertObjectToJson(Object o) {
        try {
            return OBJECT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}