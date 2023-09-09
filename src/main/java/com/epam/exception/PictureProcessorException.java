package com.epam.exception;

public class PictureProcessorException extends RuntimeException {

   public PictureProcessorException() {
   }

   public PictureProcessorException(String message) {
      super(message);
   }

   public PictureProcessorException(String message, Throwable cause) {
      super(message, cause);
   }

   public PictureProcessorException(Throwable cause) {
      super(cause);
   }
}
