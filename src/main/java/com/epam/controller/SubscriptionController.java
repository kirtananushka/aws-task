package com.epam.controller;

import com.epam.model.Subscriber;
import com.epam.service.SubscriberService;
import com.epam.service.dto.SubscriberDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class SubscriptionController {

   private final SubscriberService subscriberService;

   @GetMapping("/showSubscribeForm")
   public String showSubscriptionForm() {
      return "subscribeForm";
   }

   @PostMapping("/subscribe")
   public String subscribe(@ModelAttribute SubscriberDto subscriberDto, Model model) {
      Subscriber subscribed = subscriberService.subscribe(subscriberDto);
      model.addAttribute("email", subscribed.getEmail());
      return "confirmationPage";
   }

   @GetMapping("/subscribers")
   public String listSubscribers(Model model) {
      List<SubscriberDto> subscribers = subscriberService.findAll();
      model.addAttribute("subscribers", subscribers);
      return "subscribersList";
   }

   @PostMapping("/unsubscribe")
   public String unsubscribe(@RequestParam String email) {
      subscriberService.unsubscribe(email);
      return "redirect:/subscribers";
   }

   @PostMapping("/unsubscribeAll")
   public String unsubscribeAll() {
      subscriberService.unsubscribeAll();
      return "redirect:/subscribers";
   }

   @PostMapping("/clearSubscribersDb")
   public String deleteAllFromDb() {
      subscriberService.deleteAllFromDb();
      return "redirect:/subscribers";
   }
}
