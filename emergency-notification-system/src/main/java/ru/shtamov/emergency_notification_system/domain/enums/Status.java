package ru.shtamov.emergency_notification_system.domain.enums;

/**
 * Перечисление, которое статус нотификации
 * (необходим для повторной проверки базы данных на случай, если нотификация не была отправлена)
 * */
public enum Status {
    NOT_SENT,
    SENT
}
