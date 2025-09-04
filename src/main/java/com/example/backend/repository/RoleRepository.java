package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Trova un ruolo per nome
     */
    Optional<Role> findByName(String name);
    
    /**
     * Verifica se esiste un ruolo con il nome specificato
     */
    boolean existsByName(String name);
}
