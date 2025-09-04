package com.example.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Email(message = "Formato email non valido")
    @NotBlank(message = "L'email è obbligatoria")
    @Size(max = 190, message = "L'email non può superare i 190 caratteri")
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank(message = "Lo username è obbligatorio")
    @Size(max = 100, message = "Lo username non può superare i 100 caratteri")
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 6, message = "La password deve avere almeno 6 caratteri")
    @Column(name = "password_hash", nullable = false)
    private String password;
    
    @Size(max = 100, message = "Il nome non può superare i 100 caratteri")
    @Column(name = "first_name")
    private String firstName;
    
    @Size(max = 100, message = "Il cognome non può superare i 100 caratteri")
    @Column(name = "last_name")
    private String lastName;
    
    @Size(max = 50, message = "Il telefono non può superare i 50 caratteri")
    private String phone;
    
    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;
    
    @Column(name = "is_active", nullable = false)
    private Boolean enabled = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cart> carts;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
    
    // Metodo di convenienza per ottenere il ruolo principale
    public Role getRole() {
        return roles.isEmpty() ? null : roles.iterator().next();
    }
    
    // Metodo di convenienza per impostare un ruolo principale
    public void setRole(Role role) {
        this.roles.clear();
        if (role != null) {
            this.roles.add(role);
        }
    }
}
