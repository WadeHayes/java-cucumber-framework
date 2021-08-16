package net.wade.autotests.core;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JSON {

    /**
     * Сравнение двух JSON с параметрами
     *
     * @param json_1 - Передаем сюда путь к первому JSON или сам JSON
     * @param json_2 - Передаем сюда путь ко второму JSON или сам JSON
     * @param mode - Тип сравнения
     */
    public static void validateJSONs(String json_1, String json_2, JSONCompareMode mode) {
        if (json_1 == null) {
            throw new RuntimeException("Пожалуйста укажите 'json_1', чтобы провести сравнение");
        } else {
            if (json_2 == null) {
                throw new RuntimeException("'response' является null");
            } else {
                String reqRs;
                String expectedRs;

                if (new File(json_1).isFile()) {
                    try {
                        byte[] encoded = Files.readAllBytes(new File(json_1).toPath());
                        reqRs = new String(encoded, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        reqRs = json_1;
                    }
                } else {
                    reqRs = json_1;
                }

                if (new File(json_2).isFile()) {
                    try {
                        byte[] encoded = Files.readAllBytes(new File(json_2).toPath());
                        expectedRs = new String(encoded, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        expectedRs = json_2;
                    }
                } else {
                    expectedRs = json_2;
                }

                try {
                    JSONAssert.assertEquals(reqRs, expectedRs, mode);
                } catch (JSONException var6) {
                    throw new RuntimeException(var6);
                }
            }
        }
    }

    /**
     * Сравнение двух JSON
     *
     * @param json_1 - Передаем сюда первый JSON
     * @param json_2 - Передаем сюда путь ко второму JSON или сам JSON
     */
    public static void validateJSONs(String json_1, String json_2) {
        validateJSONs(json_1, json_2, JSONCompareMode.NON_EXTENSIBLE);
    }
}
