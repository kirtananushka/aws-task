package com.epam.service;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.epam.config.AmazonProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class LambdaService {

    private final AWSLambda awsLambda;

    private final AmazonProperties amazonProperties;

    public void invokeLambda() {
        InvokeRequest request = new InvokeRequest().withFunctionName(amazonProperties.getLambdaArn())
              .withPayload("{\"detail-type\": \"Kir's application\"}");
        log.info("Lambda was invoked");
        awsLambda.invoke(request);
    }
}
