package com.ivzh.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Map;

public class UserRequestHandler implements RequestHandler<Map<String,String>, String> {
    @Override
    public String handleRequest(Map<String, String> map, Context context) {
        StringBuilder sb = new StringBuilder();
        map.put("default", "default");
        for (Map.Entry<String, String> e : map.entrySet()) {
            System.out.println(e.getKey() + " " + e.getValue());
            sb.append(e.getKey() + " " + e.getValue());
        }

        return "OK " + sb.toString();
    }
}
