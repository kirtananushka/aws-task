package com.epam.controller;

import com.epam.service.BucketService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
@Slf4j
public class BucketController {

   private final BucketService bucketService;

   @GetMapping("/allOperations")
   public String showAllOperations(Model model) {
      return "allOperations";
   }

   @GetMapping("/getBucketsList")
   public String viewBuckets(Model model) {
      model.addAttribute("bucketsList", bucketService.getBucketsList());
      return "buckets";
   }

   @PostMapping("/createBucket")
   public String createBucket() {
      bucketService.createBucket();
      return "redirect:/getBucketsList";
   }

   @PostMapping("/deleteBucket")
   public String deleteBucket() {
      bucketService.deleteBucket();
      return "redirect:/getBucketsList";
   }
}
