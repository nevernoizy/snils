package com.example.demo.controllers;

import com.example.demo.models.*;
import com.example.demo.validate.CheckSnils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class JsonController {


    @PostMapping("/snils")
    public ResponseEntity<?> snilsRequest(@RequestBody Map<String, Object> body) {

        // 1. Проверяем структуру JSON на наличие правильного ключа "snils"
        if (!body.containsKey("snils") || body.get("snils") == null) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("message", "Error: uncorrected json");
            errorResponse.put("request", body); // Возвращаем сырой некорректный json
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String snils = body.get("snils").toString();

        // 2. Валидация формата и контрольной суммы СНИЛС
        if (!CheckSnils.validate(snils)) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("message", "Error: uncorrected snils");
            errorResponse.put("snils", snils);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 3. Успешный ответ (Код 200)
        Map<String, Object> successResponse = new LinkedHashMap<>();
        successResponse.put("message", "success");
        successResponse.put("snils", snils);
        return ResponseEntity.ok(successResponse);
    }


    private ResponseEntity<JSONmodel> prepareResponse(JSONmodel request) {

        return ResponseEntity.ok(request);
    }
}
