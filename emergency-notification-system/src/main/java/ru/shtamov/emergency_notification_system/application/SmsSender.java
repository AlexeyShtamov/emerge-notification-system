package ru.shtamov.emergency_notification_system.application;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

import ru.shtamov.emergency_notification_system.domain.Notification;
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer;
import se.akerfeldt.okhttp.signpost.SigningInterceptor;

/**
 * Сервис для отправки нотификации по sms при помощи gatewayapi
 */
@Service
public class SmsSender {

    @Value("${sms.key}")
    private String key;

    @Value("${sms.secret}")
    private String secret;

    /**
     * Метод для отправки нотификации по sms
     * @param notification сама нотификация
     * @throws OAuthMessageSignerException выбрасывается при ошибки авторизации на сервисе gatewayapi
     */
    public void sendSms(Notification notification) throws IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
        OkHttpOAuthConsumer consumer = new OkHttpOAuthConsumer(key, secret);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new SigningInterceptor(consumer))
                .build();
        JSONObject json = new JSONObject();
        json.put("sender", notification.getTitle());
        json.put("message", notification.getText());
        json.put("recipients", (new JSONArray()).put(
                (new JSONObject()).put("msisdn", Long.parseLong(notification.getPhoneNumber()))
        ));

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"), json.toString());
        Request signedRequest = (Request) consumer.sign(
                new Request.Builder()
                        .url("https://gatewayapi.com/rest/mtsms")
                        .post(body)
                        .build()).unwrap();

        try (Response response = client.newCall(signedRequest).execute()) {
            System.out.println(response.body().string());
        }
    }
}
