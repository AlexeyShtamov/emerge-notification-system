package ru.shtamov.emergency_notification_system.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.shtamov.emergency_notification_system.domain.Notification;

/**
 * Сервис для отправки нотификации на электронную почту
 */
@Service
public class EmailSender {

    @Value("${spring.mail.sender.email}")
    private String senderMail;

    private final JavaMailSender javaMailSender;

    public EmailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Метод для отпаврки нотификации на почту
     * @param notification сама нотификация
     */
    public void sendEmail(Notification notification) {

        SimpleMailMessage smm = new SimpleMailMessage();

        smm.setFrom(senderMail);
        smm.setTo(notification.getEmail());
        smm.setSubject(notification.getTitle());
        smm.setText(notification.getText());
        javaMailSender.send(smm);
    }
}
