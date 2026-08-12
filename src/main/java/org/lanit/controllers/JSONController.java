package org.lanit.controllers;

import org.lanit.modelsJson.RequestJson;
import org.lanit.validate.CheckSnils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class JSONController {


    @PostMapping("/snils")
    public ResponseEntity<?> snilsRequest(@RequestBody Map<String, Object> body) {

        // 1. Проверяем структуру JSON на наличие правильного ключа "snils"
        if (!body.containsKey("snils") || body.get("snils") == null) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("message", "Error: uncorrected json");
            errorResponse.put("request", body); // Возвращаем сырой некорректный json
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
