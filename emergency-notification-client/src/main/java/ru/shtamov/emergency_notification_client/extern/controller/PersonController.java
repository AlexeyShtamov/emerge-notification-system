package ru.shtamov.emergency_notification_client.extern.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.shtamov.emergency_notification_client.application.CsvParseService;
import ru.shtamov.emergency_notification_client.application.PersonService;
import ru.shtamov.emergency_notification_client.domain.Person;
import ru.shtamov.emergency_notification_client.extern.DTOs.PersonInfoDTO;
import ru.shtamov.emergency_notification_client.extern.assemblers.PersonAssembler;
import ru.shtamov.emergency_notification_client.extern.exceptions.NoContentException;
import ru.shtamov.emergency_notification_client.extern.exceptions.NotCorrectFileFormatException;
import ru.shtamov.emergency_notification_client.extern.exceptions.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/people")
public class PersonController {

    private final CsvParseService csvParseService;
    private final PersonAssembler personAssembler;
    private final PersonService personService;

    public PersonController(CsvParseService csvParseService, PersonAssembler personAssembler, PersonService personService) {
        this.csvParseService = csvParseService;
        this.personAssembler = personAssembler;
        this.personService = personService;
    }

    @PostMapping
    public ResponseEntity<List<PersonInfoDTO>> postPeople(@RequestParam("file") MultipartFile multipartFile) throws IOException, NoContentException, NotCorrectFileFormatException, ParseException {
        return new ResponseEntity<>(
                csvParseService.parsePeople(multipartFile).stream().map(personAssembler::fromPersonToDTO).toList(),
        HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<PersonInfoDTO>> getAll(@RequestParam("offset") Integer offset,
                                                      @RequestParam("limit") Integer limit){
        return new ResponseEntity<>(
                personService.getAll(PageRequest.of(offset, limit)).map(personAssembler::fromPersonToDTO)
                , HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> removeById(@PathVariable Long id){
        personService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonInfoDTO> update(@RequestBody @Valid Person person, @PathVariable Long id) throws NoContentException {
        Person updatedPerson = personService.updatePersonById(Optional.of(person), id);
        return new ResponseEntity<>(personAssembler.fromPersonToDTO(updatedPerson), HttpStatus.OK);
    }
}
