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
import ru.shtamov.emergency_notification_client.application.PersonService;
import ru.shtamov.emergency_notification_client.domain.Person;
import ru.shtamov.emergency_notification_client.extern.exceptions.NoContentException;
import ru.shtamov.emergency_notification_client.extern.repositories.PersonRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSavePeople() {
        // Данные для теста
        Person person1 = new Person();
        person1.setFullName("John Doe");

        Person person2 = new Person();
        person2.setFullName("Jane Doe");

        List<Person> personList = List.of(person1, person2);

        // Настройка мока
        when(personRepository.save(any(Person.class))).thenReturn(person1);

        // Вызов метода
        personService.savePeople(personList);

        // Проверка вызова сохранения для каждого человека
        verify(personRepository, times(2)).save(any(Person.class));
    }

    @Test
    void testGetAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Person person = new Person();
        person.setFullName("John Doe");

        Page<Person> page = new PageImpl<>(List.of(person));

        when(personRepository.findAll(pageable)).thenReturn(page);

        Page<Person> result = personService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        verify(personRepository).findAll(pageable);
    }

    @Test
    void testDeleteById() {
        Long id = 1L;

        // Вызов метода
        personService.deleteById(id);

        // Проверка, что метод deleteById был вызван
        verify(personRepository).deleteById(id);
    }

    @Test
    void testUpdatePersonById_Success() throws NoContentException {
        Long id = 1L;
        Person update = new Person();
        update.setFullName("Updated Name");

        when(personRepository.save(any(Person.class))).thenReturn(update);

        // Вызов метода
        Person updatedPerson = personService.updatePersonById(Optional.of(update), id);

        assertEquals("Updated Name", updatedPerson.getFullName());
        assertEquals(id, updatedPerson.getId());
        verify(personRepository).save(update);
    }

    @Test
    void testUpdatePersonById_PersonNotFound() {
        Long id = 1L;

        assertThrows(NoContentException.class, () ->
                personService.updatePersonById(Optional.empty(), id)
        );
    }
}

