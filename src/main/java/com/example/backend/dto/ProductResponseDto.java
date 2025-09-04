package com.example.backend.dto;

import com.example.backend.model.Category;
import com.example.backend.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    
    private Long id;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer stockQty;
    private Boolean isActive;
    private String imageUrl;
    private Set<String> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Costruttore che converte da Product entity a DTO
    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.slug = product.getSlug();
        this.sku = product.getSku();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.currency = product.getCurrency();
        this.stockQty = product.getStockQty();
        this.isActive = product.getIsActive();
        this.imageUrl = product.getImageUrl();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
        
        // Converte le categorie in una Set di stringhe (evita problemi di serializzazione)
        if (product.getCategories() != null) {
            this.categories = product.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet());
        }
    }
}
