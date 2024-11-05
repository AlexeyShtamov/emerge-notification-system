package ru.shtamov.emergency_notification_client.extern.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.shtamov.emergency_notification_client.domain.Person;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findAllByFullNameIn(List<String> fullNames);
}
