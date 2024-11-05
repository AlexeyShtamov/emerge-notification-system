package ru.shtamov.emergency_notification_client.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.shtamov.emergency_notification_client.application.NotificationService;
import ru.shtamov.emergency_notification_client.application.PatternService;
import ru.shtamov.emergency_notification_client.domain.Pattern;
import ru.shtamov.emergency_notification_client.domain.Person;
import ru.shtamov.emergency_notification_client.extern.exceptions.IncorrectNotificationFormatException;
import ru.shtamov.emergency_notification_client.extern.exceptions.IsAlreadyExistException;
import ru.shtamov.emergency_notification_client.extern.exceptions.NoContentException;
import ru.shtamov.emergency_notification_client.extern.repositories.PatternRepository;
import ru.shtamov.emergency_notification_client.extern.repositories.PersonRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PatternServiceTest {

    @Mock
    private PatternRepository patternRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PatternService patternService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSavePattern_Success() throws IncorrectNotificationFormatException, IsAlreadyExistException {
        // Данные для теста
        Pattern pattern = new Pattern();
        pattern.setTitle("Test Pattern");
        pattern.setPattern("Hello, {name} from {city}!");
        List<String> peopleNames = List.of("John Doe");

        // Настройка мока
        when(patternRepository.findByTitle("Test Pattern")).thenReturn(Optional.empty());
        when(personRepository.findAllByFullNameIn(peopleNames)).thenReturn(List.of(new Person()));
        when(patternRepository.save(any(Pattern.class))).thenAnswer(invocation -> {
            Pattern savedPattern = invocation.getArgument(0);
            savedPattern.setUuid(UUID.randomUUID().toString());
            return savedPattern;
        });

        // Вызов метода
        Pattern savedPattern = patternService.savePattern(Optional.of(pattern), peopleNames);

        // Проверка результата
        assertNotNull(savedPattern.getUuid());
        verify(patternRepository).save(any(Pattern.class));
    }

    @Test
    void testSavePattern_AlreadyExists() {
        Pattern pattern = new Pattern();
        pattern.setTitle("Existing Pattern");
        pattern.setPattern("Hello, {name} from {city}!");
        List<String> peopleNames = List.of("Jane Doe");

        when(patternRepository.findByTitle("Existing Pattern")).thenReturn(Optional.of(pattern));

        assertThrows(IsAlreadyExistException.class, () ->
                patternService.savePattern(Optional.of(pattern), peopleNames)
        );
    }

    @Test
    void testSavePattern_IncorrectFormat() {
        Pattern pattern = new Pattern();
        pattern.setTitle("Incorrect Pattern");
        pattern.setPattern("Hello, missing placeholders!");

        List<String> peopleNames = List.of("Alice Doe");

        assertThrows(IncorrectNotificationFormatException.class, () ->
                patternService.savePattern(Optional.of(pattern), peopleNames)
        );
    }

    @Test
    void testSetPeople_Success() throws NoContentException {
        String title = "Pattern Title";
        Pattern pattern = new Pattern();
        pattern.setTitle(title);

        Person person = new Person();
        person.setFullName("John Doe");

        List<String> peopleNames = List.of("John Doe");
        when(patternRepository.findByTitle(title)).thenReturn(Optional.of(pattern));
        when(personRepository.findAllByFullNameIn(peopleNames)).thenReturn(List.of(person));

        List<Person> addedPeople = patternService.setPeople(peopleNames, title);

        assertEquals(1, addedPeople.size());
        assertEquals("John Doe", addedPeople.get(0).getFullName());
        verify(patternRepository).save(any(Pattern.class));
    }

    @Test
    void testSetPeople_PatternNotFound() {
        String title = "Nonexistent Pattern";
        List<String> peopleNames = List.of("Jane Doe");

        when(patternRepository.findByTitle(title)).thenReturn(Optional.empty());

        assertThrows(NoContentException.class, () ->
                patternService.setPeople(peopleNames, title)
        );
    }

    @Test
    void testSendByTitle_Success() throws NoContentException {
        String title = "Pattern Title";
        Pattern pattern = new Pattern();
        pattern.setTitle(title);

        when(patternRepository.findByTitle(title)).thenReturn(Optional.of(pattern));
        when(notificationService.createNotification(pattern)).thenReturn(List.of(1L, 2L));

        List<Long> notificationIds = patternService.sendByTitle(title);

        assertEquals(2, notificationIds.size());
        verify(notificationService).createNotification(pattern);
    }

    @Test
    void testSendByTitle_PatternNotFound() {
        String title = "Nonexistent Title";
        when(patternRepository.findByTitle(title)).thenReturn(Optional.empty());

        assertThrows(NoContentException.class, () -> patternService.sendByTitle(title));
    }

    @Test
    void testGetAllPatterns() {
        Pageable pageable = PageRequest.of(0, 10);
        Pattern pattern = new Pattern();
        Page<Pattern> page = new PageImpl<>(List.of(pattern));

        when(patternRepository.findAll(pageable)).thenReturn(page);

        Page<Pattern> result = patternService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        verify(patternRepository).findAll(pageable);
    }

    @Test
    void testDeleteByTitle() {
        String title = "Pattern to Delete";

        patternService.deleteByTitle(title);

        verify(patternRepository).deleteByTitle(title);
    }
}
