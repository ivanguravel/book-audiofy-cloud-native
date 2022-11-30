package com.ivzh.aws.polly;

import java.io.InputStream;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.*;

public class PollyHandler {

    private final AmazonPollyClient polly;

    public PollyHandler(Region region) {
        this.polly = new AmazonPollyClient();
        this.polly.setRegion(region);
    }

    public InputStream handle(String text, OutputFormat format) {
        SynthesizeSpeechRequest synthReq =
                new SynthesizeSpeechRequest().withText(text).withVoiceId(VoiceId.Salli)
                        .withOutputFormat(format);
        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
        return synthRes.getAudioStream();
    }
}
