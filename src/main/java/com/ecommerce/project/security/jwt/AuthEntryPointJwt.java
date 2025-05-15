package com.ecommerce.project.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is an implementation of {@link AuthenticationEntryPoint} used to handle
 * authentication exceptions and provide custom error responses for unauthorized access.
 * When an unauthenticated user attempts to access a secured resource, this entry point
 * is invoked to send an HTTP 401 Unauthorized response with a JSON body containing error details.
 *
 * Responsibilities:
 * - Logs unauthorized access attempts.
 * - Constructs and returns a JSON error response, including details such as status, error message,
 *   exception detail, and request path.
 *
 * This component is commonly used in applications secured with Spring Security and JWT authentication.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    /**
     * Handles unauthorized access attempts to protected resources by providing an HTTP 401 Unauthorized response.
     * Constructs a JSON response body containing details about the unauthorized access, including the status,
     * error type, exception message, and the request path.
     *
     * @param request       the {@link HttpServletRequest} instance representing the incoming HTTP request
     * @param response      the {@link HttpServletResponse} instance representing the HTTP response to be sent
     * @param authException the {@link AuthenticationException} that triggered this entry point, containing details
     *                      about the authentication failure
     * @throws IOException      if an I/O error occurs during the construction or writing of the response body
     * @throws ServletException if an error occurs during the handling of the servlet request or response
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        logger.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }

}
