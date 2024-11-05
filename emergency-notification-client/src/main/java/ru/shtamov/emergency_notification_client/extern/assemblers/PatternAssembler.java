package ru.shtamov.emergency_notification_client.extern.assemblers;

import org.springframework.stereotype.Component;
import ru.shtamov.emergency_notification_client.domain.Pattern;
import ru.shtamov.emergency_notification_client.domain.Person;
import ru.shtamov.emergency_notification_client.extern.DTOs.PatternDTO;

@Component
public class PatternAssembler {

    public PatternDTO fromPatternToDTO(Pattern pattern){
        PatternDTO patternDTO = new PatternDTO();
        patternDTO.setPattern(pattern.getPattern());
        patternDTO.setTitle(pattern.getTitle());
        patternDTO.setPeopleName(pattern.getPeople().stream().map(Person::getFullName).toList());
        return patternDTO;
    }

    public Pattern fromDTOToPattern(PatternDTO patternDTO){
        Pattern pattern = new Pattern();
        pattern.setPattern(patternDTO.getPattern());
        pattern.setTitle(patternDTO.getTitle());
        return pattern;
    }
}
