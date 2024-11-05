package ru.shtamov.emergency_notification_client.services;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import ru.shtamov.emergency_notification_client.application.CsvParseService;
import ru.shtamov.emergency_notification_client.domain.Person;
import ru.shtamov.emergency_notification_client.domain.enums.Communication;
import ru.shtamov.emergency_notification_client.extern.exceptions.NoContentException;
import ru.shtamov.emergency_notification_client.extern.exceptions.NotCorrectFileFormatException;
import ru.shtamov.emergency_notification_client.extern.exceptions.ParseException;
import ru.shtamov.emergency_notification_client.application.PersonService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class CsvParseServiceTest {

    @Mock
    private PersonService personService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private CsvParseService csvParseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testParsePeople_CsvFile() throws Exception {
        // Настройка
        File tempFile = File.createTempFile("test", ".csv");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("ФИО*,Средство связи*,Электронная почта,Номер телефона,Город*\n"
                    .getBytes());
            fos.write("Ivan Ivanov,EMAIL,ivan@test.com,+1234567890,Ekaterinburg\n"
                    .getBytes());
        }

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(tempFile.getName());
        doAnswer(invocation -> {
            File file = invocation.getArgument(0, File.class);
            try (FileInputStream in = new FileInputStream(tempFile);
                 FileOutputStream out = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
            return null;
        }).when(multipartFile).transferTo(any(File.class));

        // Вызов
        List<Person> result = csvParseService.parsePeople(multipartFile);

        // Проверка
        assertEquals(1, result.size());
        assertEquals("Ivan Ivanov", result.get(0).getFullName());
        assertEquals(Communication.EMAIL, result.get(0).getCommunication());
        assertEquals("ivan@test.com", result.get(0).getEmail());
        assertEquals("+1234567890", result.get(0).getPhoneNumber());
        assertEquals("Ekaterinburg", result.get(0).getCity());

        verify(personService, times(1)).savePeople(anyList());
    }

    @Test
    void testParsePeople_EmptyFile() {
        when(multipartFile.isEmpty()).thenReturn(true);

        assertThrows(NoContentException.class, () -> csvParseService.parsePeople(multipartFile));
        verify(personService, never()).savePeople(anyList());
    }

    @Test
    void testParsePeople_InvalidFormat() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");

        assertThrows(NotCorrectFileFormatException.class, () -> csvParseService.parsePeople(multipartFile));
        verify(personService, never()).savePeople(anyList());
    }

    @Test
    void testParseCSV_InvalidData() {
        File tempFile;
        try {
            tempFile = File.createTempFile("test_invalid", ".csv");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write("ФИО*,Средство связи*,Электронная почта,Номер телефона,Город*\n"
                        .getBytes());
                fos.write("InvalidData,InvalidCommunication,test@test.com,1234567890,TestCity\n"
                        .getBytes());
            }

            assertThrows(ParseException.class, () -> csvParseService.parseCSV(tempFile));
        } catch (IOException e) {
            fail("Error creating test file", e);
        }
    }

    @Test
    void testParsePeople_XlsFile() throws Exception {
        // Настройка тестового XLS-файла с данными
        File tempFile = File.createTempFile("test", ".xls");
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ФИО*");
            headerRow.createCell(1).setCellValue("Средство связи*");
            headerRow.createCell(2).setCellValue("Электронная почта");
            headerRow.createCell(3).setCellValue("Номер телефона");
            headerRow.createCell(4).setCellValue("Город*");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Иван Иванов");
            dataRow.createCell(1).setCellValue("EMAIL");
            dataRow.createCell(2).setCellValue("ivan@test.com");
            dataRow.createCell(3).setCellValue("+1234567890");
            dataRow.createCell(4).setCellValue("Екатеринбург");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
        }

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(tempFile.getName());
        doAnswer(invocation -> {
            File file = invocation.getArgument(0, File.class);
            try (FileInputStream in = new FileInputStream(tempFile);
                 FileOutputStream out = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
            return null;
        }).when(multipartFile).transferTo(any(File.class));

        // Вызов
        List<Person> result = csvParseService.parsePeople(multipartFile);

        // Проверка
        assertEquals(1, result.size());
        assertEquals("Иван Иванов", result.get(0).getFullName());
        assertEquals(Communication.EMAIL, result.get(0).getCommunication());
        assertEquals("ivan@test.com", result.get(0).getEmail());
        assertEquals("+1234567890", result.get(0).getPhoneNumber());
        assertEquals("Екатеринбург", result.get(0).getCity());

        verify(personService, times(1)).savePeople(anyList());
    }
}
