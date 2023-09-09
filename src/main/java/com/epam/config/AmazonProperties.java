package com.epam.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "amazon-properties")
public class AmazonProperties {

   @Value("${amazonProperties.accessKey}")
   private String accessKey;

   @Value("${amazonProperties.secretKey}")
   private String secretKey;

   @Value("${amazonProperties.accountId}")
   private String accountId;

   @Value("${amazonProperties.region}")
   private String region;

   @Value("${amazonProperties.endpointUrl}")
   private String bucketUrl;

   @Value("${amazonProperties.bucketName}")
   private String bucketName;

   @Value("${amazonProperties.bucketPolicy}")
   private String bucketPolicy;

   @Value("${amazonProperties.snsTopicArn}")
   private String snsTopicArn;

   @Value("${amazonProperties.snsTopicProtocol}")
   private String snsTopicProtocol;

   @Value("${amazonProperties.sqsQueueUrl}")
   private String sqsQueueUrl;

   @Value("${amazonProperties.lambdaArn}")
   private String lambdaArn;
}