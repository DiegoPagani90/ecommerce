package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.UserResponseDto;
import com.example.backend.model.User;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Per permettere richieste da frontend in sviluppo
public class UserController {
    
    private final UserService userService;
    
    /**
     * GET /api/users
     * Ottiene tutti gli utenti dal database
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        log.info("Richiesta GET /api/users - Recupero di tutti gli utenti");
        
        try {
            List<User> users = userService.getAllUsers();
            
            // Converte gli utenti in DTO per evitare problemi di serializzazione JSON
            List<UserResponseDto> userDtos = users.stream()
                    .map(UserResponseDto::new)
                    .toList();
            
            log.info("Restituiti {} utenti", userDtos.size());
            return ResponseEntity.ok(ApiResponse.success("Utenti recuperati con successo", userDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero degli utenti: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero degli utenti"));
        }
    }
    
    /**
     * GET /api/users/with-roles
     * Ottiene tutti gli utenti con i loro ruoli caricati
     */
    @GetMapping("/with-roles")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsersWithRoles() {
        log.info("Richiesta GET /api/users/with-roles - Recupero utenti con ruoli");
        
        try {
            List<User> users = userService.getAllUsersWithRoles();
            
            List<UserResponseDto> userDtos = users.stream()
                    .map(UserResponseDto::new)
                    .toList();
            
            log.info("Restituiti {} utenti con ruoli", userDtos.size());
            return ResponseEntity.ok(ApiResponse.success("Utenti con ruoli recuperati con successo", userDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero degli utenti con ruoli: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero degli utenti con ruoli"));
        }
    }
    
    /**
     * GET /api/users/active
     * Ottiene solo gli utenti attivi
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getActiveUsers() {
        log.info("Richiesta GET /api/users/active - Recupero utenti attivi");
        
        try {
            List<User> users = userService.getActiveUsers();
            
            List<UserResponseDto> userDtos = users.stream()
                    .map(UserResponseDto::new)
                    .toList();
            
            log.info("Restituiti {} utenti attivi", userDtos.size());
            return ResponseEntity.ok(ApiResponse.success("Utenti attivi recuperati con successo", userDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero degli utenti attivi: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero degli utenti attivi"));
        }
    }
    
    /**
     * GET /api/users/{id}
     * Ottiene un utente specifico per ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        log.info("Richiesta GET /api/users/{} - Recupero utente per ID", id);
        
        try {
            Optional<User> userOpt = userService.getUserById(id);
            
            if (userOpt.isPresent()) {
                UserResponseDto userDto = new UserResponseDto(userOpt.get());
                log.info("Utente trovato: {}", userDto.getEmail());
                return ResponseEntity.ok(ApiResponse.success("Utente trovato", userDto));
            } else {
                log.warn("Utente con ID {} non trovato", id);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Errore durante il recupero dell'utente con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero dell'utente"));
        }
    }
    
    /**
     * GET /api/users/count/active
     * Ottiene il conteggio degli utenti attivi
     */
    @GetMapping("/count/active")
    public ResponseEntity<ApiResponse<Long>> countActiveUsers() {
        log.info("Richiesta GET /api/users/count/active - Conteggio utenti attivi");
        
        try {
            Long count = userService.countActiveUsers();
            log.info("Conteggio utenti attivi: {}", count);
            return ResponseEntity.ok(ApiResponse.success("Conteggio completato", count));
            
        } catch (Exception e) {
            log.error("Errore durante il conteggio degli utenti attivi: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il conteggio"));
        }
    }
}
