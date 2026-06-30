package in.nearkart.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

@Configuration
public class KafkaConfig {

    @Value("${nearkart.kafka.topics.payment-success}") private String paymentSuccessTopic;
    @Value("${nearkart.kafka.topics.payment-failed}")  private String paymentFailedTopic;
    @Value("${nearkart.kafka.topics.refund-initiated}") private String refundInitiatedTopic;

    @Bean public NewTopic paymentSuccessTopic() {
        return TopicBuilder.name(paymentSuccessTopic).partitions(3).replicas(1).build();
    }
    @Bean public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(paymentFailedTopic).partitions(3).replicas(1).build();
    }
    @Bean public NewTopic refundInitiatedTopic() {
        return TopicBuilder.name(refundInitiatedTopic).partitions(3).replicas(1).build();
    }
    @Bean public RecordMessageConverter converter() {
        return new JsonMessageConverter();
    }
}
