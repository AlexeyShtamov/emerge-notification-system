package ru.shtamov.emergency_notification_client.extern.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.shtamov.emergency_notification_client.domain.Pattern;

import java.util.Optional;

@Repository
public interface PatternRepository extends JpaRepository<Pattern, String> {
    Optional<Pattern> findByTitle(String title);
    void deleteByTitle(String title);
}
