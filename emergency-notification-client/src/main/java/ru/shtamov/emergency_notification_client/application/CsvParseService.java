package ru.shtamov.emergency_notification_client.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.kafka.shaded.com.google.protobuf.TextFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.shtamov.emergency_notification_client.domain.Person;
import ru.shtamov.emergency_notification_client.domain.enums.Communication;
import ru.shtamov.emergency_notification_client.extern.exceptions.NoContentException;
import ru.shtamov.emergency_notification_client.extern.exceptions.NotCorrectFileFormatException;
import ru.shtamov.emergency_notification_client.extern.exceptions.ParseException;


import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для загруски пользователей при csv или xls/xlsx файла
 */
@Service
@Slf4j
public class CsvParseService {


    private final String[] HEADERS = { "ФИО*", "Средство связи*", "Электронная почта", "Номер телефона", "Город*"};

    private final PersonService personService;

    public CsvParseService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Общий метод, который определяет формат файла и высывает необходимый метод парсеровки
     * @param multipartFile сам файл
     * @return список созданных пользователей
     * @throws NoContentException если файл пустой
     * @throws IOException если проблемы с чтением из файла
     * @throws NotCorrectFileFormatException если формат файла неккоректыный
     * @throws ParseException если во время парсинга произошла ошибка
     */
    public List<Person> parsePeople(MultipartFile multipartFile) throws NoContentException, IOException, NotCorrectFileFormatException, ParseException {
        if (multipartFile.isEmpty()) throw new NoContentException("File couldn't be empty");

        File file = File.createTempFile("upload_", "_" + multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);

        String fileExtend = getExtend(file.getName());

        List<Person> personList = switch (fileExtend) {
            case ".csv" -> parseCSV(file);
            case ".xls", ".xlsx" -> parseXLS(fileExtend, file);
            default ->
                    throw new NotCorrectFileFormatException("Format couldn't be " + fileExtend + ", please, use xls, xlsx, csv formats");
        };
        log.info("Parsing file {} is ready", file.getName());

        personService.savePeople(personList);
        return personList;
    }

    /**
     * Метод для парсинга csv файла
     * @param file сам файл
     * @return список распарсенных пользователей
     * @throws IOException если проблемы с чтением из файла
     * @throws ParseException если во время парсинга произошла ошибка
     */
    public List<Person> parseCSV(File file) throws IOException, ParseException {
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8);){
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .build();
            List<Person> personList = new ArrayList<>();

            Iterable<CSVRecord> records = csvFormat.parse(reader);

            for (CSVRecord record : records){
                String fullName = record.get(0);
                Communication communication = Communication.valueOf(record.get(1).toUpperCase());
                String email = record.get(2).isEmpty() ? null : record.get(2);
                String phoneNumber = record.get(3).isEmpty() ? null : record.get(3);
                String city = record.get(4).isEmpty() ? null : record.get(4);

                personList.add(Person.builder()
                        .fullName(fullName)
                        .communication(communication)
                        .email(email)
                        .phoneNumber(phoneNumber)
                        .city(city)
                        .build());

            }
            return personList;
        }catch (Exception e){
            throw new ParseException("Couldn't parse CSV file: " + e.getMessage());
        }
    }

    /**
     * Метод для парсинга xls/xlsx файла
     * @param fileExtend расширение файла (может быть xls или xlsx)
     * @param file сам файл
     * @return список распарсенных пользователей
     * @throws IOException если проблемы с чтением из файла
     * @throws ParseException если во время парсинга произошла ошибка
     */
    private List<Person> parseXLS(String fileExtend, File file) throws ParseException {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = fileExtend.equals(".xls") ? new HSSFWorkbook(fis) : new XSSFWorkbook(fis);){
            Sheet sheet = workbook.getSheetAt(0);

            List<Person> personList = new ArrayList<>();
            boolean isFirst = true;
            for (Row row : sheet) {
                if (isFirst
                        || row == null
                        || row.getCell(0) == null
                        || row.getCell(0).getStringCellValue().isEmpty()){
                    isFirst = false;
                    continue;
                }

                Person person = parseCells(row);
                personList.add(person);
            }

            return personList;
        }catch (Exception e){
            throw new ParseException("Couldn't parse XLS file: " + e.getMessage());
        }
    }


    /**
     * Метод для получения расширения файла
     * @param fileName полное имя файла
     * @return расширение
     */
    private String getExtend(String fileName){
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    /**
     * Вспомогательный метод для парсинга xls/xlsx файлов
     * @param row строка
     * @return пользователя
     */
    private Person parseCells(Row row){
        Cell fullNameCell = row.getCell(0);
        Cell communicationCell = row.getCell(1);
        Cell emailCell = row.getCell(2);
        Cell phoneCell = row.getCell(3);
        Cell cityCell = row.getCell(4);
        Person person = new Person();


        if (fullNameCell != null && fullNameCell.getCellType() == CellType.STRING && !fullNameCell.getStringCellValue().isEmpty()) {
            person.setFullName(fullNameCell.getStringCellValue());
        }

        if (communicationCell != null && communicationCell.getCellType() == CellType.STRING && !communicationCell.getStringCellValue().isEmpty()) {
            person.setCommunication(Communication.valueOf(communicationCell.getStringCellValue().toUpperCase()));
        }

        if (emailCell != null && emailCell.getCellType() == CellType.STRING && !emailCell.getStringCellValue().isEmpty()) {
            person.setEmail(emailCell.getStringCellValue());
        }

        if (phoneCell != null) {
            if (phoneCell.getCellType() == CellType.STRING)
                person.setPhoneNumber(phoneCell.getStringCellValue());
            else if (phoneCell.getCellType() == CellType.NUMERIC){
                String phoneNumber = BigDecimal.valueOf(phoneCell.getNumericCellValue()).toPlainString();
                person.setPhoneNumber(phoneNumber);
            }
        }

        if (cityCell != null && cityCell.getCellType() == CellType.STRING && !cityCell.getStringCellValue().isEmpty()) {
            person.setCity(cityCell.getStringCellValue());
        }
        return person;
    }

}
