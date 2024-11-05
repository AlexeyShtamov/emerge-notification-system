package ru.shtamov.emergency_notification_system.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import ru.shtamov.emergency_notification_system.application.EmailSender;
import ru.shtamov.emergency_notification_system.domain.Notification;

import static org.mockito.Mockito.*;

class EmailSenderTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailSender emailSender;

    @Value("${spring.mail.sender.email}")
    private String senderMail;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendEmail() {

        Notification notification = Notification.builder()
                .email("test@example.com")
                .title("Test Subject")
                .text("Test message content")
                .build();


        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setFrom(senderMail);
        smm.setTo(notification.getEmail());
        smm.setSubject(notification.getTitle());
        smm.setText(notification.getText());


        emailSender.sendEmail(notification);


        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
