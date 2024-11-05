package ru.shtamov.emergency_notification_client.application;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.shtamov.emergency_notification_client.domain.Person;
import ru.shtamov.emergency_notification_client.extern.exceptions.NoContentException;
import ru.shtamov.emergency_notification_client.extern.repositories.PersonRepository;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с пользователями
 */
@Service
@Slf4j
@Validated
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }


    /**
     * Метод для сохранения пользователей
     * @param personList список пользователей (каждый пользователь должен быть валидным
     */
    public void savePeople(List<@Valid Person> personList){
        for (Person person : personList) {
            personRepository.save(person);
            log.info("Person {} is saved", person.getFullName());
        }
    }

    /**
     * Список полученяи всех пользователей с использованием пагинации
     * @param pageable объект для пагинации
     * @return страница с пользователями
     */
    @Cacheable(value = "people", key = "'allPeople'")
    public Page<Person> getAll(Pageable pageable){
        Page<Person> people = personRepository.findAll(pageable);
        log.info("People with page size {} is gotten", pageable.getPageSize());
        return people;
    }

    /**
     * Метод для удаления пользователя по итендификатору
     * @param id итендификатор пользователя
     */
    @CacheEvict(value = "people", key = "'allPeople'")
    public void deleteById(Long id){
        personRepository.deleteById(id);
        log.info("Person with id {} is removed", id);
    }

    /**
     * Метод для обновления пользователя по итендификатору
     * @param optionalUpdate обновленный пользователь, обернутный в optional
     * @param id итендификатор пользователя
     * @return обновленный пользователь
     * @throws NoContentException выбрасывается, если нет пользователя по данному id
     */
    @CacheEvict(value = "people", key = "'allPeople'")
    public Person updatePersonById(Optional<Person> optionalUpdate, Long id) throws NoContentException {
        if (optionalUpdate.isEmpty()) throw new NoContentException("Person couldn't be null");
        Person update = optionalUpdate.get();
        update.setId(id);
        Person updatedPerson = personRepository.save(update);
        log.info("Person with id {} is updated", id);
        return updatedPerson;
    }
}
