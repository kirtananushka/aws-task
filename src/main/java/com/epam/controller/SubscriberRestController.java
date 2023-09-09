package com.epam.controller;

import com.epam.model.Subscriber;
import com.epam.service.SubscriberService;
import com.epam.service.dto.SubscriberDto;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class SubscriberRestController {

    private final SubscriberService subscriberService;

    @PostMapping("/api/subscribe")
    public Subscriber subscribe(@RequestBody SubscriberDto subscriberDto) {
        return subscriberService.subscribe(subscriberDto);
    }

    @PostMapping("/api/subscribeList")
    public List<SubscriberDto> subscribeList(@RequestBody List<SubscriberDto> subscriberDtoList) {
        return subscriberService.subscribeList(subscriberDtoList);
    }

    @DeleteMapping("/api/unsubscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@RequestBody SubscriberDto subscriberDto) {
        subscriberService.unsubscribe(subscriberDto.getEmail());
    }

    @DeleteMapping("/api/unsubscribeAll")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribeAll() {
        subscriberService.unsubscribeAll();
    }

    @DeleteMapping(value = "/api/clearSubscribersDb")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllFromDb() {
        subscriberService.deleteAllFromDb();
    }

    @GetMapping("/api/subscribers")
    public List<SubscriberDto> findAll() {
        return subscriberService.findAll();
    }
}
