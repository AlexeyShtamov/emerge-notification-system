package ru.shtamov.emergency_notification_client.extern.exceptions;


public class IncorrectNotificationFormatException extends Exception{

    public IncorrectNotificationFormatException(String message) {
        super(message);
    }
}
