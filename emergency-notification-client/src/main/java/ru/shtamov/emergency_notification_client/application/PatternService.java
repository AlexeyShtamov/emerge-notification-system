package ru.shtamov.emergency_notification_client.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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

/**
 * Сервис для работы с паттернами
 */
@Service
@Slf4j
public class PatternService {

    private final PatternRepository patternRepository;
    private final PersonRepository personRepository;
    private final NotificationService notificationService;

    public PatternService(PatternRepository patternRepository, PersonRepository personRepository, NotificationService notificationService) {
        this.patternRepository = patternRepository;
        this.personRepository = personRepository;
        this.notificationService = notificationService;
    }

    /**
     * Метод для создания паттерна
     * @param optionalPattern сам паттерн обернутый в optional
     * @param peopleNames список с именами людей, кому этот паттерн будет принадлежать
     * @return сам паттерн
     * @throws IncorrectNotificationFormatException выкидывается, если паттерн не имеет {name} и {city}
     * @throws IsAlreadyExistException выкидывается если паттерн с таким название уже существует
     */

    public Pattern savePattern(Optional<Pattern> optionalPattern, List<String> peopleNames) throws IncorrectNotificationFormatException, IsAlreadyExistException {

        if (optionalPattern.isEmpty()) throw new NullPointerException("Pattern couldn't be null");
        Pattern pattern = optionalPattern.get();

        if (isCorrectPattern(pattern)){
            if (patternRepository.findByTitle(pattern.getTitle()).isPresent())
                throw new IsAlreadyExistException("Pattern with title " + pattern.getTitle() + " is already exist");

            pattern.setUuid(generateUUID());
            List<Person> people = personRepository.findAllByFullNameIn(peopleNames);
            for (Person person : people){
                pattern.getPeople().add(person);
            }
            patternRepository.save(pattern);
            log.info("Pattern {} is saved", pattern.getTitle());
            return pattern;
        }
        throw new IncorrectNotificationFormatException("Your pattern should have {name} and {city} in text, also titleName and senderName");
    }

    /**
     * Метод для названия паттерну людей, которые уже есть базе данных
     * @param peopleNames список имен этих людей
     * @param title название паттерна
     * @return список людей
     * @throws NoContentException выкидывается если нет паттерна с данным названием
     */
    @CacheEvict(value = "patterns", key = "#title")
    public List<Person> setPeople(List<String> peopleNames, String title) throws NoContentException {
        Optional<Pattern> optionalPattern = patternRepository.findByTitle(title);

        if (optionalPattern.isPresent()){
            Pattern pattern = optionalPattern.get();

            List<Person> people = personRepository.findAllByFullNameIn(peopleNames);
            List<Person> pattenPeople = pattern.getPeople();
            for (Person person : people){
                if (!pattenPeople.contains(person)) pattern.getPeople().add(person);
            }
            patternRepository.save(pattern);

            log.info("To pattern {}, added next people: {}", title, people);
            return people;
        }
        throw new NoContentException("Pattern with title " + title + " doesn't exist");
    }

    /**
     * Метод для отправки нотификации по определенному паттерну всем пользователям, которые на него подписаны
     * @param title название паттерна
     * @return список итендификаторов нотификации в базе данных
     * @throws NoContentException выкидывается если нет паттерна с данным названием
     */
    @Cacheable(value = "patterns", key = "#title")
    public List<Long> sendByTitle(String title) throws NoContentException {
        Optional<Pattern> optionalPattern = patternRepository.findByTitle(title);
        if (optionalPattern.isPresent()){
            Pattern pattern = optionalPattern.get();
            List<Long> notificationsId = notificationService.createNotification(pattern);
            log.info("Notifications is created using pattern {}", pattern.getTitle());
            return notificationsId;
        }
        throw new NoContentException("Pattern with title " + title + " doesn't exist");
    }

    /**
     * Метод для получения всех паттернов
     * @param pageable объект для пагинации
     * @return страницу с паттернами
     */
    @Cacheable(value = "allPatterns", key = "'allPattern'")
    public Page<Pattern> getAll(Pageable pageable){
        Page<Pattern> patterns = patternRepository.findAll(pageable);
        log.info("Patterns with page size {} is gotten", pageable.getPageSize());
        return patterns;
    }

    /**
     * Метод для удаления паттерна по названию
     * @param title название
     */
    @Caching(evict = {
            @CacheEvict(value = "allPatterns", key = "'allPattern'"),
            @CacheEvict(value = "patterns", key = "#title")
    })
    public void deleteByTitle(String title){
        patternRepository.deleteByTitle(title);
        log.info("Person with title {} is removed", title);
    }

    /**
     * Метод для проверки корректности паттерна
     * @param pattern сам паттерн
     * @return true - если коррекнтый, false - если нет
     */
    private boolean isCorrectPattern(Pattern pattern){
        String patternValue = pattern.getPattern();

        int nameIndex = patternValue.lastIndexOf("{name}");
        int cityIndex = patternValue.lastIndexOf("{city}");

        return nameIndex != -1 && cityIndex != -1;
    }

    /**
     * Метод для генирации уникального UUID
     * @return UUID
     */
    private String generateUUID(){
        return UUID.randomUUID().toString();
    }



}
