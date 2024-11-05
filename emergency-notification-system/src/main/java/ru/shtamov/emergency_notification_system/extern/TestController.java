package ru.shtamov.emergency_notification_system.extern;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.shtamov.emergency_notification_system.application.SmsSender;
import ru.shtamov.emergency_notification_system.domain.Notification;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/test")
public class TestController {


    private final SmsSender smsService;

    public TestController(SmsSender smsService) {
        this.smsService = smsService;
    }

}
