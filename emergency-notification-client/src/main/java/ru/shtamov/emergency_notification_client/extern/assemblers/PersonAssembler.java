package ru.shtamov.emergency_notification_client.extern.assemblers;

import org.springframework.stereotype.Component;
import ru.shtamov.emergency_notification_client.domain.Person;
import ru.shtamov.emergency_notification_client.extern.DTOs.PersonInfoDTO;

@Component
public class PersonAssembler {


    public PersonInfoDTO fromPersonToDTO(Person person){
        PersonInfoDTO personInfoDTO = new PersonInfoDTO();
        personInfoDTO.setFullName(person.getFullName());
        personInfoDTO.setCommunication(String.valueOf(person.getCommunication()));
        personInfoDTO.setCity(person.getCity());
        return personInfoDTO;
    }
}
