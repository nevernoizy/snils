package org.lanit.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lanit.modelsJson.RequestJson;
import org.lanit.validate.CheckSnils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@ResponseBody
public class JSONController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/snils")
    public ResponseEntity<?> snilsRequest(@RequestBody String rawBody) throws IOException {

        // 1. Считаем количество упоминаний ключа "snils" в сыром тексте запроса
        Pattern pattern = Pattern.compile("\"snils\"\\s*:");
        Matcher matcher = pattern.matcher(rawBody);
        
        int snilsOccurrences = 0;
        while (matcher.find()) {
            snilsOccurrences++;
        }

        // Парсим в Map для проверки внутренней структуры первого уровня
        Map<String, Object> body;
        try {
            body = objectMapper.readValue(rawBody, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception e) {
            // Если JSON синтаксически сломан или прислали массив вместо объекта — 400
            return buildErrorResponse(rawBody);
        }

        // Если JSON пустой, содержит лишние ключи, дубликаты или не содержит snils вообще — это 400
        if (body.isEmpty() || snilsOccurrences > 1 || body.size() > 1 || !body.containsKey("snils") || body.get("snils") == null) {
            return buildErrorResponse(rawBody);
        }

        String snils = body.get("snils").toString();

        // 2. Валидация формата и контрольной суммы СНИЛС
        if (!CheckSnils.validate(snils)) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("message", "Error: uncorrected snils");
            errorResponse.put("snils", snils);
            return ResponseEntity.badRequest().header("Content-Type", "application/json").body(errorResponse);
        }

        // 3. Успешный ответ (Код 200)
        Map<String, Object> successResponse = new LinkedHashMap<>();
        successResponse.put("message", "success");
        successResponse.put("snils", snils);
        return ResponseEntity.ok().header("Content-Type", "application/json").body(successResponse);
    }

        // Вспомогательный метод для сборки ответа с ошибкой на основе СЫРОЙ строки запроса
    private ResponseEntity<?> buildErrorResponse(String rawBody) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("message", "Error: uncorrected json");

        // 1. Очищаем от переносов строк (\n, \r) и заменяем их на пустые строки
        String flatString = rawBody.replaceAll("[\\n\\r]", "");

        // 2. Очищаем сырой JSON от кавычек
        String noQuotes = flatString.replace("\"", "");

        // 3. Убираем лишние пробелы вокруг двоеточий, приводя к виду "ключ: значение"
        String formattedString = noQuotes.replaceAll("\\s*:\\s*", ": ");

        // 4. Очищаем пробелы вокруг запятых и принудительно ставим ",  " (два пробела, как в Expected)
        formattedString = formattedString.replaceAll("\\s*,\\s*", ",  ");

        // 5. Очищаем пробелы внутри фигурных скобок по краям
        formattedString = formattedString.replaceAll("\\{\\s*", "{").replaceAll("\\s*\\}", "}");

        // 6. Подставляем ровно два обязательных пробела в начало после открывающей фигурной скобки
        String customJsonString = "{  " + formattedString.substring(1);

        errorResponse.put("request", customJsonString);

        return ResponseEntity.badRequest()
                .header("Content-Type", "application/json")
                .body(errorResponse);
    }


    private ResponseEntity<RequestJson> prepareResponse(RequestJson request) {
        return ResponseEntity.ok(request);
    }
}
