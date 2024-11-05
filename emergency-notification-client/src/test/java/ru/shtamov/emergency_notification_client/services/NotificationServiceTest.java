package ru.shtamov.emergency_notification_client.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import ru.shtamov.emergency_notification_client.application.NotificationService;
import ru.shtamov.emergency_notification_client.domain.Notification;
import ru.shtamov.emergency_notification_client.domain.Pattern;
import ru.shtamov.emergency_notification_client.domain.Person;
import ru.shtamov.emergency_notification_client.domain.enums.Communication;
import ru.shtamov.emergency_notification_client.domain.enums.Status;
import ru.shtamov.emergency_notification_client.extern.repositories.NotificationRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cacheManager = new ConcurrentMapCacheManager("notifications");
    }

    @Test
    void testCreateNotification_Success() {
        // Настройка данных
        Pattern pattern = new Pattern();
        pattern.setTitle("Test Notification");
        pattern.setPattern("Hello, {name} from {city}!");

        Person person1 = new Person();
        person1.setFullName("Alex Smith");
        person1.setCity("New York");
        person1.setEmail("alex@example.com");
        person1.setPhoneNumber("+123456789");
        person1.setCommunication(Communication.EMAIL);

        Person person2 = new Person();
        person2.setFullName("John Doe");
        person2.setCity("Los Angeles");
        person2.setEmail("john@example.com");
        person2.setPhoneNumber("+987654321");
        person2.setCommunication(Communication.SMS);

        List<Person> people = List.of(person1, person2);
        pattern.setPeople(people);

        Notification notification1 = Notification.builder()
                .id(1L)
                .title("Test Notification")
                .email("alex@example.com")
                .phoneNumber("+123456789")
                .communication("EMAIL")
                .status(Status.NOT_SENT)
                .text("Hello, Alex Smith from New York!")
                .build();

        Notification notification2 = Notification.builder()
                .id(2L)
                .title("Test Notification")
                .email("john@example.com")
                .phoneNumber("+987654321")
                .communication("PHONE")
                .status(Status.NOT_SENT)
                .text("Hello, John Doe from Los Angeles!")
                .build();

        // Настройка поведения мока
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification1)
                .thenReturn(notification2);

        // Вызов тестируемого метода
        List<Long> notificationIds = notificationService.createNotification(pattern);

        // Проверка результатов
        assertEquals(2, notificationIds.size());
        assertEquals(1L, notificationIds.get(0));
        assertEquals(2L, notificationIds.get(1));

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void testCreateNotification_TextReplacement() {
        Pattern pattern = new Pattern();
        pattern.setTitle("Personalized Notification");
        pattern.setPattern("Dear {name}, welcome to {city}!");

        Person person = new Person();
        person.setFullName("Jane Doe");
        person.setCity("San Francisco");
        person.setEmail("jane@example.com");
        person.setPhoneNumber("+555666777");
        person.setCommunication(Communication.EMAIL);

        List<Person> people = List.of(person);
        pattern.setPeople(people);

        Notification expectedNotification = Notification.builder()
                .id(1L)
                .title("Personalized Notification")
                .email("jane@example.com")
                .phoneNumber("+555666777")
                .communication("EMAIL")
                .status(Status.NOT_SENT)
                .text("Dear Jane Doe, welcome to San Francisco!")
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(expectedNotification);

        List<Long> notificationIds = notificationService.createNotification(pattern);

        assertEquals(1, notificationIds.size());
        assertEquals(1L, notificationIds.get(0));

        verify(notificationRepository, times(1)).save(argThat(notification ->
                "Dear Jane Doe, welcome to San Francisco!".equals(notification.getText())
        ));
    }

    @Test
    void testCreateNotification_Cacheable() {
        // Настройка данных
        Pattern pattern = new Pattern();
        pattern.setTitle("Cached Notification");
        pattern.setPattern("Hello, {name}!");
        Person person = new Person();
        person.setFullName("Sam Johnson");
        person.setCity("Austin");
        person.setEmail("sam@example.com");
        person.setPhoneNumber("+333444555");
        person.setCommunication(Communication.EMAIL);
        pattern.setPeople(List.of(person));

        Notification notification = Notification.builder()
                .id(3L)
                .title("Cached Notification")
                .email("sam@example.com")
                .phoneNumber("+333444555")
                .communication("EMAIL")
                .status(Status.NOT_SENT)
                .text("Hello, Sam Johnson!")
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Первый вызов - уведомление должно сохраняться и кэшироваться
        List<Long> firstCallIds = notificationService.createNotification(pattern);
        assertEquals(1, firstCallIds.size());
        assertEquals(3L, firstCallIds.get(0));

        // Второй вызов - уведомление должно браться из кэша
        List<Long> secondCallIds = notificationService.createNotification(pattern);
        assertEquals(firstCallIds, secondCallIds);

        // Проверка, что notificationRepository.save был вызван только один раз
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
