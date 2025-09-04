package com.example.backend.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.LoginRequestDto;
import com.example.backend.dto.LoginResponseDto;
import com.example.backend.dto.RegisterRequestDto;
import com.example.backend.dto.UserResponseDto;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final String INTERNAL_SERVER_ERROR = "Errore interno del server";
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    /**
     * POST /api/auth/register
     * Registra un nuovo utente
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody RegisterRequestDto request) {
        log.info("Tentativo di registrazione per utente: {}", request.getEmail());
        
        try {
            // Verifica se l'email è già in uso
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email già in uso"));
            }
            
            // Verifica se l'username è già in uso
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username già in uso"));
            }
            
            // Trova il ruolo USER
            Optional<Role> userRoleOpt = roleRepository.findByName("USER");
            if (userRoleOpt.isEmpty()) {
                return ResponseEntity.internalServerError()
                        .body(ApiResponse.error("Ruolo USER non trovato nel sistema"));
            }
            
            // Crea il nuovo utente
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setRole(userRoleOpt.get());
            user.setEnabled(true);
            
            User savedUser = userRepository.save(user);
            
            UserResponseDto userDto = new UserResponseDto(savedUser);
            return ResponseEntity.ok(ApiResponse.success("Utente registrato con successo", userDto));
            
        } catch (Exception e) {
            log.error("Errore durante la registrazione: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * POST /api/auth/login
     * Effettua il login di un utente
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Tentativo di login per utente: {}", request.getUsername());
        
        try {
            // Autentica l'utente
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Trova l'utente
            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Utente non trovato"));
            }
            
            User user = userOpt.get();
            
            // Genera il token JWT
            String token = jwtService.generateToken(user.getUsername());
            
            LoginResponseDto loginResponse = new LoginResponseDto(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().getName()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Login effettuato con successo", loginResponse));
            
        } catch (Exception e) {
            log.error("Errore durante il login: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Credenziali non valide"));
        }
    }
    
    /**
     * POST /api/auth/logout
     * Effettua il logout (invalida il token)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("Richiesta logout");
        
        try {
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(ApiResponse.success("Logout effettuato con successo", null));
            
        } catch (Exception e) {
            log.error("Errore durante il logout: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/auth/me
     * Ottiene le informazioni dell'utente corrente
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser() {
        log.info("Richiesta informazioni utente corrente");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Utente non autenticato"));
            }
            
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Utente non trovato"));
            }
            
            UserResponseDto userDto = new UserResponseDto(userOpt.get());
            return ResponseEntity.ok(ApiResponse.success("Informazioni utente recuperate", userDto));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero delle informazioni utente: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * POST /api/auth/check-email
     * Verifica se un'email è disponibile
     */
    @PostMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(@RequestBody String email) {
        log.info("Verifica disponibilità email: {}", email);
        
        try {
            boolean available = !userRepository.existsByEmail(email);
            return ResponseEntity.ok(ApiResponse.success("Verifica completata", available));
            
        } catch (Exception e) {
            log.error("Errore durante la verifica dell'email: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * POST /api/auth/check-username
     * Verifica se un username è disponibile
     */
    @PostMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(@RequestBody String username) {
        log.info("Verifica disponibilità username: {}", username);
        
        try {
            boolean available = !userRepository.existsByUsername(username);
            return ResponseEntity.ok(ApiResponse.success("Verifica completata", available));
            
        } catch (Exception e) {
            log.error("Errore durante la verifica dell'username: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
}
