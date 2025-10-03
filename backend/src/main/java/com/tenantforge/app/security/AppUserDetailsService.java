package com.tenantforge.app.security;

import com.tenantforge.app.domain.AppUser;
import com.tenantforge.app.repository.AppUserRepository;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public AppUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository
                .findByEmailIgnoreCase(username)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new AppUserPrincipal(user);
    }

    public static class AppUserPrincipal implements UserDetails {
        private final AppUser user;
        private final Collection<? extends GrantedAuthority> authorities;

        public AppUserPrincipal(AppUser user) {
            this.user = user;
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }

        public AppUser user() {
            return user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return !user.isDeleted();
        }

        @Override
        public boolean isAccountNonLocked() {
            return !user.isDeleted();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return !user.isDeleted();
        }

        @Override
        public boolean isEnabled() {
            return !user.isDeleted();
        }
    }
}
