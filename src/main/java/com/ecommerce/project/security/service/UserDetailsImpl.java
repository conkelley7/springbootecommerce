package com.ecommerce.project.security.service;

import com.ecommerce.project.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link UserDetails} interface
 * that provides core user information for Spring Security. This class contains user-related details
 * such as id, username, email, password, and authorities, which represent the user's roles.
 * It serves as the model to hold authentication and authorization data.
 *
 * This class includes methods required by the UserDetails interface for evaluating user-specific
 * account status like account expiration, lock status, and credentials validity, always returning
 * true for simplicity in this implementation.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Converts a User entity into a UserDetailsImpl object, which implements the UserDetails interface
     * for use with Spring Security. This method maps user roles to authorities and includes other
     * necessary user details.
     *
     * @param user the User entity containing user information such as username, email, roles, and password
     * @return a UserDetailsImpl object that contains the user's id, username, email, password, and authorities
     */
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());
        return new UserDetailsImpl(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
