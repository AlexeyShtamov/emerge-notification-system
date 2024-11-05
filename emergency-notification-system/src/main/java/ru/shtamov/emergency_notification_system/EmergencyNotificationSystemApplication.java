package ru.shtamov.emergency_notification_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EmergencyNotificationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmergencyNotificationSystemApplication.class, args);
	}

}
