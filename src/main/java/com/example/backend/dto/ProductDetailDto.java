package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.backend.model.Category;
import com.example.backend.model.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDto {
    
    private Long id;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer stockQty;
    private Boolean isActive;
    private String imageUrl; // Solo URL principale per l'immagine, il frontend gestisce il resto
    private Boolean inStock;
    private String availability;
    private List<CategoryDto> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Costruttore che converte da Product entity a DTO dettagliato
    public ProductDetailDto(Product product) {
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
        
        // Calcola disponibilità
        this.inStock = product.getStockQty() != null && product.getStockQty() > 0;
        this.availability = calculateAvailability(product.getStockQty());
        
        // Converte le categorie in DTO
        if (product.getCategories() != null) {
            this.categories = product.getCategories().stream()
                    .map(CategoryDto::new)
                    .toList();
        }
    }
    
    private String calculateAvailability(Integer stock) {
        if (stock == null || stock <= 0) {
            return "Non disponibile";
        } else if (stock <= 5) {
            return "Ultimi pezzi disponibili";
        } else if (stock <= 10) {
            return "Pochi pezzi disponibili";
        } else {
            return "Disponibile";
        }
    }
    
    // DTO interno per Category
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private Long id;
        private String name;
        private String slug;
        private String description;
        
        public CategoryDto(Category category) {
            this.id = category.getId();
            this.name = category.getName();
            this.slug = category.getSlug();
            this.description = category.getDescription();
        }
    }
}
