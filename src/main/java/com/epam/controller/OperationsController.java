package com.epam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OperationsController {

   @GetMapping("/")
   public String redirect() {
      return "redirect:/operations";
   }

   @GetMapping("/operations")
   public String operations() {
      return "operations";
   }
}
