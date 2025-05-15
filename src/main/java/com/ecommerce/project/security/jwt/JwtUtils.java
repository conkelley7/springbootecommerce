package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${spring.app.jwtCookieName}")
    private String jwtCookie;

    /**
     * Retrieves the JSON Web Token (JWT) stored in the cookies of the provided HTTP request.
     * This method looks for a cookie with the name specified in the `jwtCookie` field,
     * extracts its value, and returns it as the JWT.
     *
     * @param request the {@link HttpServletRequest} containing cookies from which the JWT
     *                is to be extracted
     * @return the JWT string if the cookie is present and contains a value, or null if
     *         the cookie is absent or has no value
     */
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    /**
     * Generates a JSON Web Token (JWT) cookie for the specified user principal.
     * This method creates a JWT using the username from the provided user details
     * and configures it into an HTTP response cookie with a specified path and expiration time.
     *
     * @param userPrincipal the {@link UserDetailsImpl} object containing the user's information
     *                      used to generate the JWT. The username from this principal is used as
     *                      the subject of the JWT.
     * @return a {@link ResponseCookie} object containing the generated JWT, with the configured path
     *         and expiration settings.
     */
    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateTokenFromUsername(userPrincipal);
        return ResponseCookie.from(jwtCookie, jwt)
                .path("/api")
                .maxAge(jwtExpirationMs)
                .build();
    }

    /**
     * Creates a clean JSON Web Token (JWT) cookie with no value.
     * For use in logout functionality to replace cookie with JWT.
     *
     * @return a {@link ResponseCookie} object representing the empty JWT cookie configured
     *         for the "/api" path.
     */
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookie)
                .path("/api")
                .build();
    }

    /**
     * Generates a JSON Web Token (JWT) for the given user details.
     * The token contains the username as its subject, the issued date, and an expiration
     * date based on the configured expiration time. The token is signed using a secret key.
     *
     * @param userDetails the {@link UserDetails} object containing the user's information,
     *                    specifically the username used as the token subject
     * @return a JWT string representing the user's authentication token
     */
    public String generateTokenFromUsername(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    /**
     * Extracts the username (subject) from a given JSON Web Token (JWT).
     * The method parses the JWT, verifies its signature with the provided secret key,
     * and retrieves the subject (username) from the payload.
     *
     * @param token the JSON Web Token (JWT) string from which the username is to be extracted
     * @return the username (subject) contained within the JWT
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    /**
     * Generates a secret key to be used for signing and verifying JSON Web Tokens (JWTs).
     * The key is derived using the configured `jwtSecret` by decoding its Base64 representation.
     *
     * @return a {@link Key} object representing the secret key for HMAC-SHA signing
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Validates the given JSON Web Token (JWT). This method checks the token's
     * integrity, expiration, structure, and compliance with the configured secret key.
     * If the token is valid, the method returns true. Otherwise, it logs the corresponding
     * exception and returns false.
     *
     * @param authToken the JSON Web Token (JWT) string to be validated
     * @return true if the token is valid; false if the token is invalid, expired, unsupported,
     *         or if the claims string is empty
     */
    public boolean validateJwtToken(String authToken) {
        try {
            System.out.println("Validate");
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
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
