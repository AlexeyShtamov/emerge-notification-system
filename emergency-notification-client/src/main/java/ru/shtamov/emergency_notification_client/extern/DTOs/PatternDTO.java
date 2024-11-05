package ru.shtamov.emergency_notification_client.extern.DTOs;

import lombok.Data;

import java.util.List;

@Data
public class PatternDTO {

    private String title;
    private String pattern;
    private List<String> peopleName;

}
