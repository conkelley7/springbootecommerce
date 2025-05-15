package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.service.UserDetailsServiceImpl;
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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A filter that intercepts each HTTP request to authenticate users based on
 * a JSON Web Token (JWT) included in the request header. The filter extracts
 * and validates the JWT, retrieves user details, and sets authentication in
 * the security context if the token is valid.
 */
@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    /**
     * Processes incoming HTTP requests to authenticate users based on the presence
     * of a JSON Web Token (JWT) in the request. The method attempts to extract, validate,
     * and parse the JWT. If the token is valid, it retrieves the user details, sets up
     * the authentication object, and stores it in the Security Context.
     *
     * @param request the HttpServletRequest object that represents the incoming request
     * @param response the HttpServletResponse object that represents the outgoing response
     * @param filterChain the FilterChain object used to pass the request and response
     *                     to the next filter or rest of the processing pipeline
     * @throws ServletException if the filter encounters an issue during execution
     * @throws IOException if there is an I/O error in the processing of the request or response
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null,
                                userDetails.getAuthorities());
                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts a JSON Web Token (JWT) from the incoming HTTP request's "Authorization" header.
     * The method retrieves the JWT string by utilizing a utility method to parse the header.
     * Logs the extracted JWT for debugging purposes and returns it.
     *
     * @param request the {@link HttpServletRequest} object containing the "Authorization" header
     *                from which the JWT will be extracted
     * @return the JWT string if present in the header and properly formatted, or null if no JWT
     *         is found or the header does not meet the expected format
     */
    private String parseJwt(HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromCookies(request);
        logger.debug("AuthTokenFilter.java: {}", jwt);
        return jwt;
    }
}
