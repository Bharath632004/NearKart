package com.nearkart.userservice.messaging;

import com.nearkart.userservice.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private static final String TOPIC_USER_REGISTERED = "user.registered";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        kafkaTemplate.send(TOPIC_USER_REGISTERED, String.valueOf(event.userId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish UserRegisteredEvent for userId={}: {}",
                                event.userId(), ex.getMessage());
                    } else {
                        log.info("Published UserRegisteredEvent for userId={}", event.userId());
                    }
                });
    }
}
