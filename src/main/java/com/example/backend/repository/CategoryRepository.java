package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.backend.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Trova tutte le categorie attive
     */
    List<Category> findByActiveTrue();
    
    /**
     * Trova una categoria per ID se è attiva
     */
    Optional<Category> findByIdAndActiveTrue(Long id);
    
    /**
     * Verifica se esiste una categoria con il nome specificato (case insensitive)
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Cerca categorie per nome (case insensitive) tra quelle attive
     */
    List<Category> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    
    /**
     * Trova le categorie principali (senza parent) attive
     */
    List<Category> findByParentIsNullAndActiveTrue();
    
    /**
     * Trova le sottocategorie di una categoria padre
     */
    List<Category> findByParentIdAndActiveTrue(Long parentId);
    
    /**
     * Trova tutte le categorie con prodotti attivi
     */
    @Query("SELECT DISTINCT c FROM Category c " +
           "JOIN c.products p " +
           "WHERE c.active = true AND p.active = true")
    List<Category> findCategoriesWithActiveProducts();
    
    /**
     * Conta le categorie attive
     */
    long countByActiveTrue();
}
