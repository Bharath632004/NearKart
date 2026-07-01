package com.nearkart.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.user-registered:user-registered}")
    private String userRegisteredTopic;

    @Value("${kafka.topic.user-deactivated:user-deactivated}")
    private String userDeactivatedTopic;

    public void publishUserRegistered(String userId, String email, String name, String phone) {
        Map<String, Object> event = Map.of(
                "eventType", "USER_REGISTERED",
                "userId", userId,
                "email", email,
                "name", name,
                "phone", phone,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(userRegisteredTopic, userId, event);
        log.info("Published USER_REGISTERED event for userId {}", userId);
    }

    public void publishUserDeactivated(String userId, String email) {
        Map<String, Object> event = Map.of(
                "eventType", "USER_DEACTIVATED",
                "userId", userId,
                "email", email,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(userDeactivatedTopic, userId, event);
        log.info("Published USER_DEACTIVATED event for userId {}", userId);
    }
}
