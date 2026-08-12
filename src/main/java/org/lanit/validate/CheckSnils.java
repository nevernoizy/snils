package org.lanit.validate;

import java.util.regex.Pattern;

public class CheckSnils {

    public static boolean validate(String snils){

            if (snils == null) {
                return false;
            }

            // 1. Проверяем строгий формат маски: "123-456-789 12"
            // \\d{3} - три цифры, затем дефис, три цифры, дефис, три цифры, пробел и две цифры
            if (!Pattern.matches("^\\d{3}-\\d{3}-\\d{3} \\d{2}$", snils)) {
                return false;
            }

            // 2. Очищаем строку для расчета (гарантированно останутся только 11 цифр)
            String cleanSnils = snils.replaceAll("-| ", "");

            // 3. Проверка номеров меньше или равных 001-001-998
            long snilsNumber = Long.parseLong(cleanSnils.substring(0, 9));
            if (snilsNumber <= 1001998L) {
                return true;
            }

            // 4. Расчет контрольной суммы
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += (cleanSnils.charAt(i) - '0') * (9 - i);
            }

            // 5. Определение контрольного числа
            int expectedControl = sum % 101;
            if (expectedControl == 100 || expectedControl == 101) {
                expectedControl = 0;
            }

            // 6. Сравнение с хвостом СНИЛС
            int actualControl = Integer.parseInt(cleanSnils.substring(9));
            return expectedControl == actualControl;
        }

    }

