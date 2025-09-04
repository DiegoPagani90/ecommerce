package com.example.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Il nome del prodotto è obbligatorio")
    @Size(max = 200, message = "Il nome non può superare i 200 caratteri")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Lo slug è obbligatorio")
    @Size(max = 220, message = "Lo slug non può superare i 220 caratteri")
    @Column(unique = true, nullable = false)
    private String slug;
    
    @NotBlank(message = "Lo SKU è obbligatorio")
    @Size(max = 100, message = "Lo SKU non può superare i 100 caratteri")
    @Column(unique = true, nullable = false)
    private String sku;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Il prezzo è obbligatorio")
    @DecimalMin(value = "0.01", message = "Il prezzo deve essere maggiore di 0")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    
    @Size(max = 3, message = "La valuta deve essere di 3 caratteri")
    @Column(nullable = false)
    private String currency = "EUR";
    
    @Column(name = "stock_qty", nullable = false)
    private Integer stockQty = 0;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Size(max = 500, message = "L'URL dell'immagine non può superare i 500 caratteri")
    @Column(name = "image_url")
    private String imageUrl;
    
    @ManyToMany
    @JoinTable(
        name = "product_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();
    

    
    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItems;
    
    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
