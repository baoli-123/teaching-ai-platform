package com.example.teachingai.controller;

import com.example.teachingai.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/locale")
@RequiredArgsConstructor
public class LocaleController {

    private final MessageSource messageSource;

    @GetMapping
    public ApiResponse<Map<String, String>> locale(
            @RequestParam(required = false) String lang,
            HttpServletResponse response
    ) {
        String cookieValue = "zh";
        if ("en".equalsIgnoreCase(lang)) {
            cookieValue = "en";
        }
        response.addHeader("Set-Cookie", "lang=" + cookieValue + "; Path=/; Max-Age=2592000; SameSite=Lax");
        Locale locale = "en".equalsIgnoreCase(lang) ? Locale.ENGLISH : Locale.SIMPLIFIED_CHINESE;
        return ApiResponse.ok(Map.of(
                "locale", locale.getLanguage(),
                "message", messageSource.getMessage("locale.updated", null, locale)
        ));
    }
}
