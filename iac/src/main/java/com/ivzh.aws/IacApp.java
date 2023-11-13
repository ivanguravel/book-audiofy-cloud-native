package com.ivzh.aws;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class IacApp {
    public static void main(final String[] args) {
        App app = new App();

        new IacStack(app, "IacStack", StackProps.builder()
                .build());

        app.synth();
    }
}

