package ru.shtamov.emergency_notification_client.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import ru.shtamov.emergency_notification_client.domain.enums.Communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сущность пользователя, которому будут отправляться нотификации
 * (пользователь может иметь только одна средство коммуницаии)
 */
@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class Person implements Serializable {

    /** Итендификатор пользователя */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Полное имя пользователя */
    @NotNull(message = "Couldn't be null")
    @NotBlank(message = "Couldn't be blank")
    private String fullName;

    /** Средство коммуникации с пользователем
     * @see Communication
     */
    @Enumerated(EnumType.STRING)
    private Communication communication;

    /** Электронная почта пользователя */
    @Email(message = "Illegal format")
    private String email;

    /** Номер телефона пользователя */
    private String phoneNumber;

    /** Город пользователя */
    @NotNull(message = "Couldn't be null")
    @NotBlank(message = "Couldn't be blank")
    private String city;

    /** Список паттернов, на основе которых пользователю будут отправляться нотификации*/
    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<Pattern> patterns = new ArrayList<>();


    @Override
    public String toString() {
        return "Person{" +
                "fullName='" + fullName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(fullName, person.fullName) && communication == person.communication && Objects.equals(city, person.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, communication, city);
    }
}
