package com.ecommerce.project.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Deze class is bedoeld om binnenkomende requests te onderscheppen, de JWT in de header te valideren en de
// authentication context te zetten indien de token geldig is
// De extension zorgt ervoor dat de filter niet meer dan 1x per request aangeroepen wordt
@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    // Utils die wij gecreëerd hebben om te helpen bij het valideren van de JWT token
    @Autowired
    private JwtUtils jwtUtils;

    // TODO: Wat is en doet deze service
    @Autowired
    private UserDetailsService userDetailsService;

    // Logger voor persoonlijk gebruik
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    // Onderdeel van de OncePerRequestFilter
    // Filtert het request eenmalig per request
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());

        try {
            // Het request wordt ontleed om de JWT token te extraheren uit de header van het request
            String jwt = jwtUtils.getJwtFromHeader(request);
            logger.debug("AuthTokenFilter.java: {}", jwt);

            // Hieronder gebeurt het valideren van de binnengekomen token
            // Als de token bestaat en het een valid token is
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Haal de username op
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // Laad de user details aan de hand van de username
                // TODO: Waar worden de user details vandaan gehaald?
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Een token wordt gebouwd aan de hand van de JWT token. Deze wordt gebruikt binnen spring security
                // Wachtwoord is niet nodig omdat wij willen controleren of deze gebruiker toegang heeft to een
                // bepaald endpoint. Daarvoor is de gebruiker al ingelogd en dus al geverifieerd
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );

                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());
                // Voegt extra informatie (details_ toe aan de authentication token
                // TODO: waar is dit nuttig voor
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContextHolder is de context die Spring Security gebruikt. De autentication token die
                // hierboven wordt aangemaakt, wordt in de context geplaatst. Spring weet dan dat deze user toegang
                // mag hebben tot de applicatie
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        // Geeft de informatie door aan de volgende filter om te checken of dit een valide request en response is
        filterChain.doFilter(request, response);
    }
}
