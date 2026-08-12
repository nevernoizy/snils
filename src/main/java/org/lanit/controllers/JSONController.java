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

        // Проверяем сырой текст на количество упоминаний ключа "snils" через регулярное выражение
        Pattern pattern = Pattern.compile("\"snils\"\\s*:");
        Matcher matcher = pattern.matcher(rawBody);
        
        int snilsOccurrences = 0;
        while (matcher.find()) {
            snilsOccurrences++;
        }

        // Парсим строку в стандартную Map для проверки внутренней структуры
        Map<String, Object> body;
        try {
            body = objectMapper.readValue(rawBody, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception e) {
            // Если упало на этапе парсинга (значит прислали массив или битый синтаксис) — сразу 400
            return buildErrorResponse(new LinkedHashMap<>());
        }

        // Условие падения теста: пустой JSON, больше одного ключа "snils" в тексте,
        // общий размер карты больше 1 (лишние ключи) или отсутствие правильного поля.
        if (body.isEmpty() || snilsOccurrences > 1 || body.size() > 1 || !body.containsKey("snils") || body.get("snils") == null) {
            return buildErrorResponse(body);
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

    // Вспомогательный метод для сборки кривого формата JSON с двумя пробелами
    private ResponseEntity<?> buildErrorResponse(Map<String, Object> body) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("message", "Error: uncorrected json");

        String mapString = body.toString();
        String formattedString = mapString.replace("=", ": ");
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
