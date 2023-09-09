package com.epam.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.epam.config.AmazonProperties;
import com.epam.exception.PictureProcessorException;
import com.epam.model.PictureMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MessageService {

   private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

   private final AmazonSNS snsClient;

   private final AmazonSQS sqsClient;

   private final AmazonProperties amazonProperties;

   public void sendSqsMessage(PictureMetadata metadata) {
      String serializedMetadata = getSerializedMetadata(metadata);
      SendMessageRequest sendMessageRequest = new SendMessageRequest().withQueueUrl(amazonProperties.getSqsQueueUrl())
            .withMessageBody(serializedMetadata);
      sqsClient.sendMessage(sendMessageRequest);
   }

//   @Scheduled(fixedRate = 60000)
//   public void receiveAndProcessMessagesFromQueue() {
//      String queueUrl = amazonProperties.getSqsQueueUrl();
//
//      List<Message> messages = sqsClient.receiveMessage(queueUrl).getMessages();
//
//      log.info("Starting scheduled task for {} messages", messages.size());
//
//      for (Message message : messages) {
//         String serializedMetadata = message.getBody();
//         PictureMetadata metadata = getDeserializedMetadata(serializedMetadata);
//         sendSnsMessage(metadata);
//         sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
//      }
//      log.info("Scheduled task executed");
//   }

   private void sendSnsMessage(PictureMetadata metadata) {
      String title = "[Picture App] New picture uploaded: " + metadata.getName();
      String message = String.format("File `%s` was uploaded.%nSize: %s%nUrl: %s", metadata.getName(),
            metadata.getSize(), metadata.getUrl());
      snsClient.publish(amazonProperties.getSnsTopicArn(), message, title);
   }

   private String getSerializedMetadata(PictureMetadata metadata) {
      try {
         return objectMapper.writeValueAsString(metadata);
      } catch (JsonProcessingException e) {
         throw new PictureProcessorException("Error while serializing metadata", e);
      }
   }

   private PictureMetadata getDeserializedMetadata(String serializedMetadata) {
      try {
         return objectMapper.readValue(serializedMetadata, PictureMetadata.class);
      } catch (JsonProcessingException e) {
         throw new PictureProcessorException("Error while deserializing metadata", e);
      }
   }
}
