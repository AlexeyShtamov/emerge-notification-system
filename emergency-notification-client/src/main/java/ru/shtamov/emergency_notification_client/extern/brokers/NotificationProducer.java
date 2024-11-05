package ru.shtamov.emergency_notification_client.extern.brokers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class NotificationProducer {

    @Value("${topic.notification}")
    private String TOPIC;

    private final KafkaTemplate<String, Long> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, Long> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(Long notificationId) {
        kafkaTemplate.send(TOPIC, notificationId);
    }
}
