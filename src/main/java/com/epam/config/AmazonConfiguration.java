package com.epam.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class AmazonConfiguration {

   private final AmazonProperties amazonProperties;

   @Bean
   public AmazonS3 amazonS3Client() {
      return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(getAWSCredentials()))
            .withRegion(amazonProperties.getRegion()).build();
   }

   @Bean
   public AmazonSNS amazonSNSClient() {
      return AmazonSNSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(getAWSCredentials()))
            .withRegion(amazonProperties.getRegion()).build();
   }

   @Bean
   public AmazonSQS amazonSQSClient() {
      return AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(getAWSCredentials()))
            .withRegion(amazonProperties.getRegion()).build();
   }

   @Bean
   public AWSLambda awsLambdaClient() {
      return AWSLambdaClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(getAWSCredentials()))
            .withRegion(amazonProperties.getRegion()).build();
   }

   private AWSCredentials getAWSCredentials() {
      return new BasicAWSCredentials(amazonProperties.getAccessKey(), amazonProperties.getSecretKey());
   }
}
