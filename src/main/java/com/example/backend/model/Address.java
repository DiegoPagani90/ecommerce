package com.example.backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Size(max = 100, message = "Il label non può superare i 100 caratteri")
    private String label;
    
    @Size(max = 200, message = "Il nome completo non può superare i 200 caratteri")
    @Column(name = "full_name")
    private String fullName;
    
    @NotBlank(message = "L'indirizzo è obbligatorio")
    @Size(max = 200, message = "L'indirizzo non può superare i 200 caratteri")
    private String line1;
    
    @Size(max = 200, message = "L'indirizzo secondario non può superare i 200 caratteri")
    private String line2;
    
    @NotBlank(message = "La città è obbligatoria")
    @Size(max = 100, message = "La città non può superare i 100 caratteri")
    private String city;
    
    @Size(max = 100, message = "Lo stato non può superare i 100 caratteri")
    private String state;
    
    @NotBlank(message = "Il CAP è obbligatorio")
    @Size(max = 30, message = "Il CAP non può superare i 30 caratteri")
    @Column(name = "postal_code")
    private String postalCode;
    
    @NotBlank(message = "Il paese è obbligatorio")
    @Size(max = 2, message = "Il codice paese deve essere di 2 caratteri")
    private String country;
    
    @Size(max = 50, message = "Il telefono non può superare i 50 caratteri")
    private String phone;
    
    @Column(name = "is_default_shipping")
    private Boolean isDefaultShipping = false;
    
    @Column(name = "is_default_billing")
    private Boolean isDefaultBilling = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
