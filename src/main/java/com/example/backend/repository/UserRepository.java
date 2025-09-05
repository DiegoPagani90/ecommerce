package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.backend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Query method per trovare utente per email
    Optional<User> findByEmail(String email);
    
    // Query method per trovare utente per username
    Optional<User> findByUsername(String username);
    
    // Query method per trovare utenti attivi
    List<User> findByEnabledTrue(); //
    
    // Query HQL personalizzata per ottenere tutti gli utenti con i loro ruoli
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles")
    List<User> findAllWithRoles();
    
    // Query HQL per contare utenti attivi
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    Long countActiveUsers();
    
    // Verifica se esiste un utente con l'email specificata
    boolean existsByEmail(String email);
    
    // Verifica se esiste un utente con l'username specificato
    boolean existsByUsername(String username);
}
