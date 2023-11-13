package com.ivzh.aws;

import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class IacStack extends Stack {
    public IacStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public IacStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Bucket bucket = Bucket.Builder.create(this, "MyBucket")
                .versioned(true)
                .encryption(BucketEncryption.KMS_MANAGED)
                .build();
    }
}
