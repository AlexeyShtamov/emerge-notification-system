package ru.shtamov.emergency_notification_system.services;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.shtamov.emergency_notification_system.application.EmailSender;
import ru.shtamov.emergency_notification_system.application.NotificationService;
import ru.shtamov.emergency_notification_system.application.SmsSender;
import ru.shtamov.emergency_notification_system.domain.Notification;
import ru.shtamov.emergency_notification_system.domain.enums.Status;
import ru.shtamov.emergency_notification_system.extern.repositories.NotificationRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private SmsSender smsSender;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetNotification_Found() {
        Long id = 1L;
        Notification notification = Notification.builder().id(id).title("Test Notification").build();

        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

        Optional<Notification> result = notificationService.getNotification(id);

        assertTrue(result.isPresent());
        assertEquals(notification, result.get());
        verify(notificationRepository).findById(id);
    }

    @Test
    void testGetNotification_NotFound() {
        Long id = 1L;
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Notification> result = notificationService.getNotification(id);

        assertTrue(result.isEmpty());
        verify(notificationRepository).findById(id);
    }

    @Test
    void testAsyncNotificationCheck() throws Exception {
        Notification notification1 = Notification.builder()
                .status(Status.NOT_SENT)
                .communication("EMAIL")
                .build();

        Notification notification2 = Notification.builder()
                .status(Status.NOT_SENT)
                .communication("SMS")
                .build();

        List<Notification> notifications = List.of(notification1, notification2);

        when(notificationRepository.findAll()).thenReturn(notifications);

        notificationService.asyncNotificationCheck();

        verify(emailSender, times(1)).sendEmail(notification1);
        verify(smsSender, times(1)).sendSms(notification2);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void testSend_Email() throws Exception {
        Notification notification = Notification.builder()
                .communication("EMAIL")
                .status(Status.NOT_SENT)
                .build();

        notificationService.send(notification);

        verify(emailSender).sendEmail(notification);
        verify(notificationRepository).save(notification);
        assertEquals(Status.SENT, notification.getStatus());
    }

    @Test
    void testSend_SMS() throws Exception {
        Notification notification = Notification.builder()
                .communication("SMS")
                .status(Status.NOT_SENT)
                .build();

        notificationService.send(notification);

        verify(smsSender).sendSms(notification);
        verify(notificationRepository).save(notification);
        assertEquals(Status.SENT, notification.getStatus());
    }

    @Test
    void testChangeStatus() throws OAuthMessageSignerException, OAuthExpectationFailedException, IOException, OAuthCommunicationException {
        Notification notification = Notification.builder()
                .title("Test Notification")
                .communication("SMS")
                .status(Status.NOT_SENT)
                .build();

        notificationService.send(notification);

        assertEquals(Status.SENT, notification.getStatus());
        verify(notificationRepository).save(notification);
    }
}
