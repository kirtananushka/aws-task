package com.epam.controller;

import com.epam.service.LambdaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class LambdaController {

   private final LambdaService lambdaService;

   @PostMapping("/notify")
   public String sendBatchNotifications() {
      lambdaService.invokeLambda();
      return "notificationConfirmation";
   }
}
