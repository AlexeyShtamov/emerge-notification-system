package ru.shtamov.emergency_notification_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class EmergencyNotificationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmergencyNotificationClientApplication.class, args);
	}

}
