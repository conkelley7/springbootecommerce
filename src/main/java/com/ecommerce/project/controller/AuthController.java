package com.ecommerce.project.controller;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Rest Controller for handling authentication-related operations such as user login and registration.
 * All endpoints in this controller are prefixed with "/api/auth".
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    /**
     * Authenticates a user based on the provided login request.
     * If the authentication is successful, a JSON Web Token (JWT) along with user details and roles
     * is returned in the response. If authentication fails, an error response indicating bad credentials is returned.
     *
     * @param loginRequest an instance of {@code LoginRequest} containing the username and password provided by the user
     * @return a {@code ResponseEntity} which contains either a {@code UserInfoResponse} object with authentication details
     *         or an error response with bad credentials
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }

    /**
     * Registers a new user with the provided signup details. The method validates the uniqueness
     * of the username and email, encrypts the user's password, assigns roles based on the input,
     * and saves the user to the repository. If the username or email already exists, an appropriate
     * error response is returned.
     *
     * @param signupRequest an instance of {@code SignupRequest} containing the user's username, email,
     *                      password, and roles
     * @return a {@code ResponseEntity} containing a success message if the user is registered
     *         successfully, or an error message if the username or email is already taken
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername()))
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken"));

        if (userRepository.existsByEmail(signupRequest.getEmail()))
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already taken"));

        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword())
        );

        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                    role = role.toLowerCase();
                    switch(role) {
                        case "admin":
                           Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                   .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                           roles.add(adminRole);
                           break;
                        case "seller":
                            Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                            roles.add(sellerRole);
                            break;
                        default:
                            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                            roles.add(userRole);
                    }});
        }
        user.setRoles(roles);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully"));

    }

    /**
     * Retrieves the current authenticated user's username.
     *
     * @param authentication an {@code Authentication} object representing the current security context
     * @return the username of the authenticated user. Returns an empty string if the authentication object is null
     */
    @GetMapping("/username")
    public String currentUsername(Authentication authentication) {
        if (authentication != null)
            return authentication.getName();
        else
            return "";
    }

    /**
     * Retrieves the details of the currently authenticated user, including their ID, username, and roles.
     *
     * @param authentication an {@code Authentication} object representing the security context of the currently authenticated user
     * @return a {@code ResponseEntity} containing a {@code UserInfoResponse} object with the user's ID, username, and roles, along with an HTTP status code of OK
     */
    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserDetails(Authentication authentication) {
        if (authentication == null) throw new APIException("No user currently logged in");

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();

        UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You have been signed out"));
    }




}
