package com.example.backend.service;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Caricamento utente per login: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Utente non trovato nel database: {}", username);
                    return new UsernameNotFoundException("Utente non trovato: " + username);
                });

        log.info("Utente trovato: {} - Enabled: {} - Roles: {}", 
            user.getUsername(), user.getEnabled(), user.getRoles().size());
        
        if (user.getRoles().isEmpty()) {
            log.warn("Utente {} non ha ruoli assegnati", username);
        }
        
        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
        log.info("Authorities per utente {}: {}", username, authorities);
        
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
                
        log.info("UserDetails creato per utente: {}", username);
        return userDetails;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        if (user.getRoles().isEmpty()) {
            log.warn("Nessun ruolo trovato per utente: {}", user.getUsername());
            return java.util.Collections.emptyList();
        }
        
        return user.getRoles().stream()
                .map(role -> {
                    log.debug("Aggiunto ruolo: {} per utente: {}", role.getName(), user.getUsername());
                    return new SimpleGrantedAuthority(role.getName());
                })
                .collect(Collectors.toList());
    }
}
