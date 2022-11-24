package com.ivzh.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.*;
import com.ivzh.aws.s3.S3OperationsHandler;

public class PollyTestS3 {

    private final AmazonPollyClient polly;
    private final S3OperationsHandler s3OperationsHandler;
    private static final String SAMPLE = "Congratulations. " +
            "You have successfully built this working demo " +
            "of Amazon Polly in Java. Have fun building voice enabled apps with Amazon Polly (that's me!), and alwayslook at the AWS website for tips and tricks on using Amazon Polly and other great services from AWS";

    public static void main(String args[]) throws Exception {
        //create the test class
        PollyTestS3 helloWorld = new PollyTestS3(Region.getRegion(Regions.US_EAST_1), new S3OperationsHandler());
        //get the audio stream
        helloWorld.synthesizeAndSave(SAMPLE, OutputFormat.Mp3, "test.mp3");
    }

    public PollyTestS3(Region region, S3OperationsHandler handler) {
        BasicAWSCredentials awsCreds = new
                BasicAWSCredentials("",
                "");

        // create an Amazon Polly client in a specific region
        this.polly = new AmazonPollyClient(new AWSStaticCredentialsProvider(awsCreds));
        this.polly.setRegion(region);
        this.s3OperationsHandler = handler;
    }

    public void synthesizeAndSave(String text, OutputFormat format, String place) throws IOException {
        SynthesizeSpeechRequest synthReq =
                new SynthesizeSpeechRequest().withText(text).withVoiceId(VoiceId.Salli)
                        .withOutputFormat(format);
        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
        InputStream audioStream = synthRes.getAudioStream();
        s3OperationsHandler.writeToS3(place, audioStream);
    }
}
