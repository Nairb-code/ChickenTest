package com.ChickenTest.demoChickenTest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.apache.log4j.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> allError(Exception ex, WebRequest req) {
        logger.error(ex.getMessage());
        return new ResponseEntity("ExceptionHandler Error " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
