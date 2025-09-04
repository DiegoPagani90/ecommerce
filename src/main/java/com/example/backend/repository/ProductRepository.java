package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Query method per trovare prodotto per slug
    Optional<Product> findBySlug(String slug);
    
    // Query method per trovare prodotto per SKU
    Optional<Product> findBySku(String sku);
    
    // Query method per trovare prodotti attivi
    List<Product> findByIsActiveTrue();
    
    // Query method per trovare prodotti per nome (case insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Query method per trovare prodotti attivi per nome
    List<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    // Query HQL personalizzata per ricerca avanzata (nome, descrizione, SKU)
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "p.isActive = true")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);
    
    // Query per prodotti con categorie caricate
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.categories WHERE p.isActive = true")
    List<Product> findAllActiveWithCategories();
    
    // Query per trovare prodotti per categoria
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id = :categoryId AND p.isActive = true")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
    
    // Query per contare prodotti attivi
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true")
    Long countActiveProducts();
    
    // Query per prodotti con stock disponibile
    List<Product> findByStockQtyGreaterThanAndIsActiveTrue(Integer minStock);
    
    // Query per prodotto con categorie caricate (senza immagini)
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.categories " +
           "WHERE p.id = :productId AND p.isActive = true")
    Optional<Product> findByIdWithCategories(@Param("productId") Long productId);
    
    // Query per prodotto per slug con categorie caricate (senza immagini)
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.categories " +
           "WHERE p.slug = :slug AND p.isActive = true")
    Optional<Product> findBySlugWithCategories(@Param("slug") String slug);
    
    // Query per prodotti per categoria con paginazione (metodo Spring Data)
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id = :categoryId AND p.isActive = true")
    org.springframework.data.domain.Page<Product> findByCategoryIdAndActiveTrue(@Param("categoryId") Long categoryId, org.springframework.data.domain.Pageable pageable);
    
    // Conta prodotti per categoria
    @Query("SELECT COUNT(p) FROM Product p JOIN p.categories c WHERE c.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
    // Conta prodotti attivi per categoria
    @Query("SELECT COUNT(p) FROM Product p JOIN p.categories c WHERE c.id = :categoryId AND p.isActive = true")
    long countByCategoryIdAndActiveTrue(@Param("categoryId") Long categoryId);
    
    // Trova prodotti con stock minore o uguale a una soglia
    List<Product> findByStockQtyLessThanEqualAndIsActiveTrue(Integer threshold);
}
