package ru.shtamov.emergency_notification_system.extern.DTOs;

import lombok.Data;

@Data
public class NotificationDTO {
    private Long id;

    private String title;
    private String text;
    private String email;
    private String phoneNumber;
    private String communication;
    private String status;
}
