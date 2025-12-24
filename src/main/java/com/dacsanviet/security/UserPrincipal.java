package com.dacsanviet.security;

import com.dacsanviet.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * UserPrincipal class implementing UserDetails
 */
public class UserPrincipal implements UserDetails {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean isActive;
    
    public UserPrincipal(Long id, String username, String email, String fullName, String password, 
                        Collection<? extends GrantedAuthority> authorities, boolean isActive) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.authorities = authorities;
        this.isActive = isActive;
    }
    
    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = new java.util.ArrayList<>();
        
        // Add role-based authorities with hierarchical permissions
        switch (user.getRole()) {
            case ADMIN:
                // ADMIN has all roles
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
            case STAFF:
                // STAFF has STAFF and USER roles
                authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
            case USER:
                // USER has only USER role
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
        }
        
        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName() != null ? user.getFullName() : user.getUsername(),
            user.getPassword(),
            authorities,
            user.getIsActive()
        );
    }
    
    public Long getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return isActive;
    }
}