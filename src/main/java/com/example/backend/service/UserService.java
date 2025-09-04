package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Ottiene tutti gli utenti dal database
     * @return Lista di tutti gli utenti
     */
    public List<User> getAllUsers() {
        log.info("Recupero di tutti gli utenti dal database");
        List<User> users = userRepository.findAll();
        log.info("Trovati {} utenti", users.size());
        return users;
    }
    
    /**
     * Ottiene tutti gli utenti con i loro ruoli caricati
     * @return Lista di utenti con ruoli
     */
    public List<User> getAllUsersWithRoles() {
        log.info("Recupero di tutti gli utenti con ruoli dal database");
        List<User> users = userRepository.findAllWithRoles();
        log.info("Trovati {} utenti con ruoli", users.size());
        return users;
    }
    
    /**
     * Ottiene solo gli utenti attivi
     * @return Lista di utenti attivi
     */
    public List<User> getActiveUsers() {
        log.info("Recupero degli utenti attivi dal database");
        List<User> activeUsers = userRepository.findByIsActiveTrue();
        log.info("Trovati {} utenti attivi", activeUsers.size());
        return activeUsers;
    }
    
    /**
     * Trova un utente per ID
     * @param id ID dell'utente
     * @return Optional contenente l'utente se trovato
     */
    public Optional<User> getUserById(Long id) {
        log.info("Ricerca utente con ID: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            log.info("Utente trovato: {}", user.get().getEmail());
        } else {
            log.warn("Nessun utente trovato con ID: {}", id);
        }
        return user;
    }
    
    /**
     * Trova un utente per email
     * @param email Email dell'utente
     * @return Optional contenente l'utente se trovato
     */
    public Optional<User> getUserByEmail(String email) {
        log.info("Ricerca utente per email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    /**
     * Conta il numero totale di utenti attivi
     * @return Numero di utenti attivi
     */
    public Long countActiveUsers() {
        log.info("Conteggio utenti attivi");
        Long count = userRepository.countActiveUsers();
        log.info("Numero utenti attivi: {}", count);
        return count;
    }
}
