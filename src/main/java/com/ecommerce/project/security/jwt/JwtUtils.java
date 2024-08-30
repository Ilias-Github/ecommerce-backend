package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

// In de header zit informatie over het type token en welk algoritme is gebruikt om de token te signen
// Signen houdt in dat je zeker wilt weten dat je weet van wie je de message ontvangen hebt en dat deze niet
// gemanipuleerd is
// In de payload zit informatie over de entiteit (in dit geval de user) die de token heeft gestuurd
// - sub: dit is de subject, oftewel de gebruiker van de token. Meestal aangeduid met de username
// - name: maakt het makkelijk om te weten van wie deze token is door een door mensen leesbare naam te hebben (hier
// - role: welke rol deze user heeft
// Signature: Wordt gebruikt om te verifieren dat de verzender daadwerkelijk is wie die zegt dat die is en zorgt
// ervoor dat de message niet aangetast is tijdens het versturen. Hierbij wordt de secret key en het algoritme
// gebruikt om de payload te signen. De payload is alleen nog maar leesbaar als de andere kant de secret key
// en dus het algoritme kan gebruiken om de correcte payload terug te krijgen
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // De secret die gebruikt wordt om de payload te signen. Deze is opgeslagen in de application.properties want
    // deze wil je nooit hard coded ergens online hebben staan.
    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    // Hoe lang de token geldig is uitgedrukt in milliseconden
    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${spring.app.jwtCookieName}")
    private String jwtCookieName;

    // Een cookie is een klein beetje data die de client ontvangt van de server met informatie over de client.
    // Bijvoorbeeld of de client is ingelogd, bepaalde voorkeuren (light/dark theme), wat er in je shopping cart zit etc
    // Haal de jwt op van de cookie die met elk request meegestuurd wordt
    public String getJwtCookie(HttpServletRequest request) {
        // Haal de cookie uit het request op met de vooraf gedefinieerde cookie naam
        // Cookies worden met elk request meegestuurd
        Cookie cookie = WebUtils.getCookie(request, jwtCookieName);
        if (cookie == null) {
            return null;
        } else {
            // Haal de jwt van de cookie op
            return cookie.getValue();
        }
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrinciple) {
        // Genereer een jwt die opgeslagen dient te worden in de cookie die teruggestuurd wordt naar de client
        String jwt = generateTokenFromUsername(userPrinciple.getUsername());

        // Maak een cookie aan met de vooral gedefinieerde naam en de gegenereerde token
        ResponseCookie cookie = ResponseCookie.from(jwtCookieName, jwt)
                // De scope waarin de cookie wordt meegestuurd. Alleen endpoints die beginnen met "/api" krijgen de
                // cookie mee
                .path("/api")
                // De cookie is maximaal 24 uur geldig (in seconden gedefinieerd)
                .maxAge(24 * 60 * 60)
                // Client side scripts mogen toegang tot de cookie(TODO: waarom?)
                .httpOnly(false)
                .build();

        return cookie;
    }

    public ResponseCookie getCleanJwtCookie() {
        ResponseCookie cookie = ResponseCookie.from(jwtCookieName, null).path("/api").build();
        return cookie;
    }

//    // Deze methode extraheert de JWT uit de header om deze terug te kunnen geven aan de aanroeper
//    // De request wordt als parameter meegegeven zodat deze ontleed kan worden. Hierbij wordt de header van het
//    // request (niet van de token) ontleed.
//    public String getJwtFromHeader(HttpServletRequest request) {
//        // De header met de naam "Authorization" wordt opgehaald van het request omdat daar de token in wordt opgeslagen
//        String bearerToken = request.getHeader("Authorization");
//        logger.debug("Authorization Header: {}", bearerToken);
//
//        // Als de bearer token niet null is en start met "Bearer " dan weten wij dat er een bearer token bestaat en
//        // dat we die moeten extraheren
//        // Bearer betekent "drager". Dus hier heb je de token van de drager
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            // Verwijder de bearer prefix inclusief de spatie
//            return bearerToken.substring(7);
//        }
//
//        return null;
//    }

    // De methode genereert aan de hand van de username een token
    public String generateTokenFromUsername(String username) {
        // UserDetails is wat Spring Security ziet als een typische user. Deze informatie heeft Spring Security nodig
        // om de requests van een user te valideren
//        String username = userDetails.getUsername();

        // Een token wordt gebouwd
        return Jwts.builder()
                .subject(username) // De sub in de payload van de token wordt gezet
                .issuedAt(new Date()) // Wanneer de token wordt aangemaakt
                // Wanneer de token ongeldig is en er een nieuwe aangemaakt dient te worden.
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key()) // Voegen een signature toe aan de token
                // Maakt de creatie van de token af door de gegeven informatie in de juiste structuur in elkaar te
                // zetten. Deze wordt dan omgezet in een URL safe String
                .compact();

    }

    // De username wordt geëxtraheerd uit de token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key()) // De token verifieren aan de hand van de vooral ingestelde key
                .build()
                .parseSignedClaims(token)
                .getPayload() // De payload uit de token extraheren
                .getSubject(); //  De subject uit de payload lezen (de subject is de username)
    }

    // De key wordt aangemaakt die nodig is bij het signen van de JWT token. Dit gebeurt met behulp van de zelf gedefinieerde secret
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Valideer de token om te achterhalen of de client toegang mag hebben tot de applicatie
    public boolean validateJwtToken(String authToken) {
        try {
            System.out.println("Validate");
            // Token validatie
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken).getPayload();
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
