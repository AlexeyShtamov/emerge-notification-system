package ru.shtamov.emergency_notification_system.extern.brokers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import ru.shtamov.emergency_notification_system.application.EmailSender;
import ru.shtamov.emergency_notification_system.application.NotificationService;
import ru.shtamov.emergency_notification_system.application.SmsSender;
import ru.shtamov.emergency_notification_system.domain.Notification;

import java.util.Optional;

@Service
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void listen(Long notificationId, Acknowledgment acknowledgment) {
        try {
            if (notificationId == null){
                throw new NullPointerException("notification id is null");
            }
            acknowledgment.acknowledge();

            Optional<Notification> notificationOptional = notificationService.getNotification(notificationId);

            if (notificationOptional.isPresent()){
                Notification notification = notificationOptional.get();
                notificationService.send(notification);
            }

            notificationService.asyncNotificationCheck();

        } catch (Exception e) {
            System.err.println("Ошибка обработки сообщения: " + e.getMessage());
        }
    }
}
