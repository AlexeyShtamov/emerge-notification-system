package ru.shtamov.emergency_notification_client.extern.DTOs;

import lombok.Data;

@Data
public class PersonInfoDTO {
    private String fullName;
    private String communication;
    private String city;
}
