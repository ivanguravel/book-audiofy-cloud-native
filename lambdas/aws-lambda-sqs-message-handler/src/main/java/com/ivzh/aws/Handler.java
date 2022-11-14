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

public class Handler implements RequestHandler<SQSEvent, Void> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        Map<String, String> map = new HashMap<>();
        for (SQSMessage msg : event.getRecords()) {
            map.putAll(msg.getAttributes());
        }

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("books-metadata");
        Item item = null;
        try {
            item = new Item().withJSON("data",OBJECT_MAPPER.writeValueAsString(map))
                    .withPrimaryKey("book", "1");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        table.putItem(item);
        return null;
    }
}