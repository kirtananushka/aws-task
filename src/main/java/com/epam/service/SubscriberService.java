package com.epam.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.epam.config.AmazonProperties;
import com.epam.exception.PictureProcessorException;
import com.epam.model.PictureMetadata;
import com.epam.model.Subscriber;
import com.epam.repository.SubscriberRepository;
import com.epam.service.dto.SubscriberDto;
import lombok.AllArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SubscriberService {

   private final SubscriberRepository subscriberRepository;

   private final AmazonSNS snsClient;

   private final AmazonProperties amazonProperties;

   public Subscriber subscribe(SubscriberDto subscriberDto) {

      if (!subscriberRepository.findByEmail(subscriberDto.getEmail()).isEmpty()) {
         throw new ServiceException("Email already exists");
      }

      SubscribeRequest subRequest = new SubscribeRequest(amazonProperties.getSnsTopicArn(),
            amazonProperties.getSnsTopicProtocol(), subscriberDto.getEmail());
      snsClient.subscribe(subRequest);

      return subscriberRepository.save(toEntity(subscriberDto));
   }

   public List<SubscriberDto> subscribeList(List<SubscriberDto> subscriberDtoList) {
      subscriberDtoList.forEach(this::subscribe);
      return findAll();
   }

   public void unsubscribe(String email) {
      Subscriber subscriber;
      try {
         subscriber = subscriberRepository.findByEmail(email).get(0);
      } catch (Exception exc) {
         throw new PictureProcessorException("Subscriber not found");
      }

      List<Subscription> subscriptions = snsClient.listSubscriptionsByTopic(amazonProperties.getSnsTopicArn())
            .getSubscriptions();
      Optional<Subscription> subscription = subscriptions.stream()
            .filter(s -> s.getEndpoint().equals(subscriber.getEmail())).findFirst();

      UnsubscribeRequest unsubRequest = new UnsubscribeRequest(subscription.get().getSubscriptionArn());
      snsClient.unsubscribe(unsubRequest);

      subscriberRepository.delete(subscriber);
   }

   public void deleteFromDb(SubscriberDto subscriberDto) {
      Subscriber subscriber;
      try {
         subscriber = subscriberRepository.findByEmail(subscriberDto.getEmail()).get(0);
      } catch (Exception exc) {
         throw new PictureProcessorException("Subscriber not found");
      }
      subscriberRepository.delete(subscriber);
   }

   public void unsubscribeAll() {
      findAll().forEach(s -> unsubscribe(s.getEmail()));
   }

   public void deleteAllFromDb() {
      findAll().forEach(this::deleteFromDb);
   }

   public List<SubscriberDto> findAll() {
      return subscriberRepository.findAll().stream().map(this::toDto).toList();
   }
//
//   public void sendMessage(PictureMetadata metadata) {
//      String title = "New picture";
//      String message = String.format("File `%s` was uploaded.%nSize: %s%nUrl: %s", metadata.getName(),
//            metadata.getSize(), metadata.getUrl());
//      snsClient.publish(amazonProperties.getSnsTopicArn(), message, title);
//   }

   private SubscriberDto toDto(Subscriber subscriber) {
      return SubscriberDto.builder().email(subscriber.getEmail()).build();
   }

   private Subscriber toEntity(SubscriberDto subscriberDto) {
      return Subscriber.builder().email(subscriberDto.getEmail()).build();
   }
}
