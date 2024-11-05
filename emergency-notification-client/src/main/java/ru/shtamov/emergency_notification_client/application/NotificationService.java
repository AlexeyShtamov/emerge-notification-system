package ru.shtamov.emergency_notification_client.application;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.shtamov.emergency_notification_client.domain.Notification;
import ru.shtamov.emergency_notification_client.domain.Pattern;
import ru.shtamov.emergency_notification_client.domain.Person;
import ru.shtamov.emergency_notification_client.domain.enums.Status;
import ru.shtamov.emergency_notification_client.extern.repositories.NotificationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с нотификациями
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Метод создания нотификации на основе паттерна
     * @param pattern сам паттерн
     * @return список итендификаторов нотификации в базе данных
     */
    public List<Long> createNotification(Pattern pattern){

        List<Long> notificationsId = new ArrayList<>();
        List<Person> people = pattern.getPeople();
        for (Person person : people){
            Notification notification = Notification.builder()
                    .title(pattern.getTitle())
                    .email(person.getEmail())
                    .phoneNumber(person.getPhoneNumber())
                    .communication(String.valueOf(person.getCommunication()))
                    .status(Status.NOT_SENT).build();

            String text = pattern.getPattern()
                    .replaceAll("\\{name}", person.getFullName())
                    .replaceAll("\\{city}", person.getCity());

            notification.setText(text);

            notificationsId.add(notificationRepository.save(notification).getId());
        }
        return notificationsId;
    }
}
