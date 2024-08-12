package com.ecommerce.project.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// De annotation onderschept elke exception die door de controllers wordt gegooid.
// De onderschepte exceptions worden afgehandeld door deze class
@RestControllerAdvice
public class MyGlobalExceptionHandler {

    // De annotation beschrijft dat de methode een exception handler is van het type "MethodArgumentNotValidException"
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // Map interface wordt gebruikt om een representatie te maken van een key-value pair.
        // Omdat Map een interface is, kan je daar geen object van maken. Daarom wordt een hashmap gemaakt
        // (deze implementeert de Map interface)
        // De eerste String betreft de veldnaam (key), de tweede de error message (value)
        // Beide values betreffen objecten
        Map<String, String> response = new HashMap<>();

        // Voor elke exception, haal de veldnaam en de error message op en sla deze op in de HashMap
        e.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = ((FieldError)err).getField();
            String message = err.getDefaultMessage();
            response.put(fieldName, message);
        });

        // ResponseEntity is een class binnen Spring die de HTTP-response representeert
        // (Status code, headers en body)
        // Dit maakt het mogelijk om de HTTP-responses te configureren
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Vangt de ResourceNotFoundException op die de controllers geven.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> myResourceNotFoundException(ResourceNotFoundException e) {
        String message = e.getMessage();
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }
}
