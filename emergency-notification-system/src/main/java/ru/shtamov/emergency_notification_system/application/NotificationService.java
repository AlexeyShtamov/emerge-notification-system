package ru.shtamov.emergency_notification_system.application;

import lombok.extern.slf4j.Slf4j;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.shtamov.emergency_notification_system.domain.Notification;
import ru.shtamov.emergency_notification_system.domain.enums.Status;
import ru.shtamov.emergency_notification_system.extern.repositories.NotificationRepository;


import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с нотификациями
 */
@Service
@Slf4j
public class NotificationService {

    private final EmailSender emailSender;
    private final SmsSender smsSender;


    private final NotificationRepository notificationRepository;

    public NotificationService(EmailSender emailSender, SmsSender smsSender, NotificationRepository notificationRepository) {
        this.emailSender = emailSender;
        this.smsSender = smsSender;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Метод для получения нотификации по итендификатору
     * @param id итендификатор нотификации
     * @return Нотификацию, обернутую в optional
     */
    @Cacheable(value = "notifications", key = "#id")
    public Optional<Notification> getNotification(Long id){
        Optional<Notification> optionalNotification = notificationRepository.findById(id);

        if (optionalNotification.isPresent()){
            Notification notification = optionalNotification.get();
            log.info("Notification with id {} is founded", id);
            return Optional.of(notification);
        }
        log.warn("Notification with id {} is not founded", id);
        return Optional.empty();
    }

    /**
     * Метод для асинхронной проверки нотификаций в бд
     */
    @Async
    public void asyncNotificationCheck() throws OAuthMessageSignerException, OAuthExpectationFailedException, IOException, OAuthCommunicationException {
        List<Notification> notificationList = notificationRepository.findAll();

        for (Notification notification : notificationList){
            if (notification.getStatus().equals(Status.NOT_SENT)){
                send(notification);
            }
        }
    }

    /**
     * Общий метод для решения куда отправить нотификацию в зависимости от способа коммуникации
     * @param notification сама нотификация
     */
    public void send(Notification notification) throws OAuthMessageSignerException, OAuthExpectationFailedException, IOException, OAuthCommunicationException {
        switch (notification.getCommunication()){
            case "EMAIL": emailSender.sendEmail(notification);
            break;
            case "SMS": smsSender.sendSms(notification);
            break;
        }
        changeStatus(notification);
    }

    /**
     * Метод для изменения статуса нотификации (с NOT_SENT на SENT)
     * @param notification сама нотификация
     */
    private void changeStatus(Notification notification){
        notification.setStatus(Status.SENT);
        log.info("Status is changed for notification with title {}", notification.getTitle());
        notificationRepository.save(notification);
    }
}
