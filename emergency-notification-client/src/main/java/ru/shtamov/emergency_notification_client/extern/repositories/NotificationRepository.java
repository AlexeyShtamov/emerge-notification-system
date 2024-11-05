package ru.shtamov.emergency_notification_client.extern.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.shtamov.emergency_notification_client.domain.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
