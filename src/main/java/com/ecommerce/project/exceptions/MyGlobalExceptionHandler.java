package com.ecommerce.project.exceptions;

import com.ecommerce.project.payload.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// De annotation onderschept elke exception die door de controllers wordt gegooid.
// De onderschepte exceptions worden door de methods in deze class afgehandeld
@RestControllerAdvice
public class MyGlobalExceptionHandler {

    // De annotation beschrijft dat de methode een exception handler is van het type "MethodArgumentNotValidException"
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // Map interface wordt gebruikt om een representatie te maken van een key-value pair.
        // Omdat Map een interface is, kan je daar geen object van maken. Daarom wordt een hashmap gemaakt. Hashmap is
        // basic implementatie van de Map interface
        // De eerste String betreft de veldnaam (key), de tweede de error message (value)
        // Beide values zijn objecten
        Map<String, String> response = new HashMap<>();

        // Voor elke exception, haal de veldnaam en de error message op en sla deze op in de HashMap
        e.getBindingResult().getAllErrors().forEach(err -> {
            // De error wordt omgezet naar een FieldError zodat wij kunnen achterhalen om welk veld het gaat
            String fieldName = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            response.put(fieldName, message);
        });

        // ResponseEntity is een class binnen Spring die de HTTP-response representeert
        // (Status code, headers en body)
        // Dit maakt het mogelijk om custom HTTP-responses te configureren
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Vangt de ResourceNotFoundException op die de controllers geven.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> myResourceNotFoundException(ResourceNotFoundException e) {
       APIResponse apiResponse = new APIResponse(e.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    // Vangt overige api exceptions op
    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> myAPIException(APIException e) {
        APIResponse apiResponse = new APIResponse(e.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
}
