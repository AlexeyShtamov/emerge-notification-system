package ru.shtamov.emergency_notification_client.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Шаблон для создания нотификаций.
 * Должнен обязательно в себя включать {name} и {city}
 * @see Notification
 */
@Entity
@Data
public class Pattern implements Serializable {

    /** Итендификатор паттерна */
    @Id
    private String uuid;

    /** Названия паттерна
     * (используется в зоголовке в письме на почту или как отправитель в смс)
     */
    @NotNull(message = "titleName couldn't be null")
    @Size(max = 10, message = "Max size of title is 10")
    private String title;

    /** Сам паттерн
     * (непосредственно текст паттерна, который д олжнен обязательно в себя включать {name} и {city})
     */
    @NotNull(message = "Text couldn't be null")
    private String pattern;

    /** Список пользователей, которым будет отправлена нотификация по данному шаблону */
    @ManyToMany
    private List<Person> people = new ArrayList<>();

}
