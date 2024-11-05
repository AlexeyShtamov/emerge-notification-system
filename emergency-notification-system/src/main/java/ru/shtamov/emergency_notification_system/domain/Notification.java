package ru.shtamov.emergency_notification_system.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.shtamov.emergency_notification_system.domain.enums.Status;

import java.io.Serializable;

/**
 * Сущность нотификации.
 * Создается из опредленного паттерна для конкретного пользователя
 */
@Data
@Entity(name = "notification")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String text;
    private String email;
    private String phoneNumber;
    private String communication;
    private Status status;
}
