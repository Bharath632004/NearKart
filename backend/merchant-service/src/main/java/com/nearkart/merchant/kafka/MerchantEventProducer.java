package com.nearkart.merchant.kafka;

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
public class MerchantEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.merchant-approved:merchant-approved}")
    private String merchantApprovedTopic;

    @Value("${kafka.topic.merchant-suspended:merchant-suspended}")
    private String merchantSuspendedTopic;

    public void publishMerchantApproved(long merchantIdBits, String merchantName, String email) {
        Map<String, Object> event = Map.of(
                "eventType", "MERCHANT_APPROVED",
                "merchantId", merchantIdBits,
                "merchantName", merchantName,
                "email", email,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(merchantApprovedTopic, String.valueOf(merchantIdBits), event);
        log.info("Published MERCHANT_APPROVED event for merchantId {}", merchantIdBits);
    }

    public void publishMerchantSuspended(long merchantIdBits, String merchantName, String email, String reason) {
        Map<String, Object> event = Map.of(
                "eventType", "MERCHANT_SUSPENDED",
                "merchantId", merchantIdBits,
                "merchantName", merchantName,
                "email", email,
                "reason", reason,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(merchantSuspendedTopic, String.valueOf(merchantIdBits), event);
        log.info("Published MERCHANT_SUSPENDED event for merchantId {}", merchantIdBits);
    }
}
