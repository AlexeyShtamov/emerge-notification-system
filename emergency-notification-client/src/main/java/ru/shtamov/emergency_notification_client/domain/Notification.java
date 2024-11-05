package ru.shtamov.emergency_notification_client.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.shtamov.emergency_notification_client.domain.enums.Status;

import java.io.Serializable;


/**
 * Сущность нотификации.
 * Создается из опредленного паттерна для конкретного пользователя
 * @see Pattern
 */
@Data
@Entity(name = "notification")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Serializable {

    /** Итендификатор нотификации */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Названия нотификации
     * (используется в зоголовке в письме на почту или как отправитель в смс)
     */
    private String title;

    /** Текст нотификации
     * Является измененным паттерном, где вместо {name} конктретное имя, вместо {city} конкретный город.
     * */
    private String text;

    /** Почта, на которую отрпавляется нотификация */
    private String email;

    /** Телефон, на который отправляется sms */
    private String phoneNumber;

    /**
     * Описывает средство натификации
     * @see ru.shtamov.emergency_notification_client.domain.enums.Communication
     */
    private String communication;

    /**
     * Статус нотификации (отправлено или нет)
     * @see Status
     */
    private Status status;
}
