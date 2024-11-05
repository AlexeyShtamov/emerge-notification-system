package ru.shtamov.emergency_notification_client.extern.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shtamov.emergency_notification_client.application.PatternService;
import ru.shtamov.emergency_notification_client.domain.Pattern;
import ru.shtamov.emergency_notification_client.domain.Person;
import ru.shtamov.emergency_notification_client.extern.DTOs.PatternDTO;
import ru.shtamov.emergency_notification_client.extern.DTOs.PersonInfoDTO;
import ru.shtamov.emergency_notification_client.extern.assemblers.PatternAssembler;
import ru.shtamov.emergency_notification_client.extern.assemblers.PersonAssembler;
import ru.shtamov.emergency_notification_client.extern.brokers.NotificationProducer;
import ru.shtamov.emergency_notification_client.extern.exceptions.IncorrectNotificationFormatException;
import ru.shtamov.emergency_notification_client.extern.exceptions.IsAlreadyExistException;
import ru.shtamov.emergency_notification_client.extern.exceptions.NoContentException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/patterns")
public class PatternController {

    private final PatternService patternService;
    private final PatternAssembler patternAssembler;
    private final PersonAssembler personAssembler;

    private final NotificationProducer notificationProducer;


    public PatternController(PatternService patternService, PatternAssembler patternAssembler, PersonAssembler personAssembler, NotificationProducer notificationProducer) {
        this.patternService = patternService;
        this.patternAssembler = patternAssembler;
        this.personAssembler = personAssembler;
        this.notificationProducer = notificationProducer;
    }

    @PostMapping
    public ResponseEntity<PatternDTO> postPattern(@RequestBody PatternDTO patternDTO) throws IncorrectNotificationFormatException, IsAlreadyExistException {
        Pattern pattern = patternAssembler.fromDTOToPattern(patternDTO);

        Pattern createdPattern =  patternService.savePattern(Optional.of(pattern), patternDTO.getPeopleName());
        return new ResponseEntity<>(
                patternAssembler.fromPatternToDTO(createdPattern),
                HttpStatus.OK);
    }


    @PutMapping("/{titleName}")
    public ResponseEntity<List<PersonInfoDTO>> setPeople(@RequestBody List<String> peopleNames, @PathVariable String titleName) throws NoContentException {
        List<Person> people = patternService.setPeople(peopleNames, titleName);

        return new ResponseEntity<>(people.stream().map(personAssembler::fromPersonToDTO).toList(), HttpStatus.OK);
    }

    @PostMapping("/{title}")
    public ResponseEntity<HttpStatus> sendAllPeopleByTitle(@PathVariable String title) throws NoContentException {
        List<Long> notificationsId = patternService.sendByTitle(title);

        for (Long id : notificationsId){
            notificationProducer.sendMessage(id);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<PatternDTO>> getAll(@RequestParam("offset") Integer offset,
                                   @RequestParam("limit") Integer limit){
        return new ResponseEntity<>(
                patternService.getAll(PageRequest.of(offset, limit)).map(patternAssembler::fromPatternToDTO)
                , HttpStatus.OK);
    }

    @DeleteMapping("/{title}")
    public ResponseEntity<HttpStatus> removeByTitle(@PathVariable String title) {
        patternService.deleteByTitle(title);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
