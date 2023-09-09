package com.epam.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Slf4j
public class ErrorController {

   @ExceptionHandler(Throwable.class)
   public ModelAndView handleException(HttpServletRequest request, Throwable e) {
      log.error("Error", e);
      ModelAndView modelAndView = new ModelAndView("error");
      modelAndView.addObject("exception", e);
      return modelAndView;
   }
}
