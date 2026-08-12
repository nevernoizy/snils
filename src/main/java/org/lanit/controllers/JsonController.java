package org.lanit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.lanit.modelsJson.RequestJson;
import org.lanit.validate.CheckSnils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@ResponseBody
public class JSONController {

    // ObjectMapper нужен для сериализации тела запроса в валидный текст JSON (Требование №11)
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/snils")
    public ResponseEntity<?> snilsRequest(@RequestBody Map<String, Object> body) throws IOException {

        // 1. Проверяем структуру JSON на наличие правильного ключа "snils"
        if (!body.containsKey("snils") || body.get("snils") == null) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("message", "Error: uncorrected json");

            // Превращаем карту в строку JSON-формата, как этого требует тест автопроверки
            String rawJsonString = objectMapper.writeValueAsString(body);
            errorResponse.put("request", rawJsonString);

            return ResponseEntity.badRequest().header("Content-Type", "application/json").body(errorResponse);
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


    private ResponseEntity<RequestJson> prepareResponse(RequestJson request) {

        return ResponseEntity.ok(request);
    }
}
