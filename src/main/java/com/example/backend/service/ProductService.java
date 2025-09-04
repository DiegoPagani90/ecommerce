package com.example.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.Product;
import com.example.backend.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    
    /**
     * Ottiene tutti i prodotti dal database
     * @return Lista di tutti i prodotti
     */
    public List<Product> getAllProducts() {
        log.info("Recupero di tutti i prodotti dal database");
        List<Product> products = productRepository.findAll();
        log.info("Trovati {} prodotti", products.size());
        return products;
    }
    
    /**
     * Ottiene tutti i prodotti attivi
     * @return Lista di prodotti attivi
     */
    public List<Product> getActiveProducts() {
        log.info("Recupero dei prodotti attivi dal database");
        List<Product> activeProducts = productRepository.findByIsActiveTrue();
        log.info("Trovati {} prodotti attivi", activeProducts.size());
        return activeProducts;
    }
    
    /**
     * Ottiene tutti i prodotti attivi con le categorie caricate
     * @return Lista di prodotti attivi con categorie
     */
    public List<Product> getActiveProductsWithCategories() {
        log.info("Recupero dei prodotti attivi con categorie dal database");
        List<Product> products = productRepository.findAllActiveWithCategories();
        log.info("Trovati {} prodotti attivi con categorie", products.size());
        return products;
    }
    
    /**
     * Trova un prodotto per ID
     * @param id ID del prodotto
     * @return Optional contenente il prodotto se trovato
     */
    public Optional<Product> getProductById(Long id) {
        log.info("Ricerca prodotto con ID: {}", id);
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            log.info("Prodotto trovato: {}", product.get().getName());
        } else {
            log.warn("Nessun prodotto trovato con ID: {}", id);
        }
        return product;
    }
    
    /**
     * Trova un prodotto per slug
     * @param slug Slug del prodotto
     * @return Optional contenente il prodotto se trovato
     */
    public Optional<Product> getProductBySlug(String slug) {
        log.info("Ricerca prodotto per slug: {}", slug);
        return productRepository.findBySlug(slug);
    }
    
    /**
     * Trova prodotti per nome (case insensitive)
     * @param name Nome da cercare
     * @return Lista di prodotti che contengono il nome
     */
    public List<Product> getProductsByName(String name) {
        log.info("Ricerca prodotti per nome: {}", name);
        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name);
        log.info("Trovati {} prodotti con nome contenente '{}'", products.size(), name);
        return products;
    }
    
    /**
     * Ricerca avanzata di prodotti (nome, descrizione, SKU)
     * @param searchTerm Termine di ricerca
     * @return Lista di prodotti che contengono il termine di ricerca
     */
    public List<Product> searchProducts(String searchTerm) {
        log.info("Ricerca avanzata prodotti con termine: {}", searchTerm);
        List<Product> products = productRepository.searchProducts(searchTerm);
        log.info("Trovati {} prodotti con termine di ricerca '{}'", products.size(), searchTerm);
        return products;
    }
    
    /**
     * Trova prodotti per categoria
     * @param categoryId ID della categoria
     * @return Lista di prodotti della categoria
     */
    public List<Product> getProductsByCategory(Long categoryId) {
        log.info("Ricerca prodotti per categoria ID: {}", categoryId);
        List<Product> products = productRepository.findByCategoryId(categoryId);
        log.info("Trovati {} prodotti nella categoria {}", products.size(), categoryId);
        return products;
    }
    
    /**
     * Trova prodotti con stock disponibile
     * @param minStock Stock minimo
     * @return Lista di prodotti con stock maggiore del minimo
     */
    public List<Product> getProductsWithStock(Integer minStock) {
        log.info("Ricerca prodotti con stock maggiore di: {}", minStock);
        List<Product> products = productRepository.findByStockQtyGreaterThanAndIsActiveTrue(minStock);
        log.info("Trovati {} prodotti con stock > {}", products.size(), minStock);
        return products;
    }
    
    /**
     * Conta il numero totale di prodotti attivi
     * @return Numero di prodotti attivi
     */
    public Long countActiveProducts() {
        log.info("Conteggio prodotti attivi");
        Long count = productRepository.countActiveProducts();
        log.info("Numero prodotti attivi: {}", count);
        return count;
    }
    
    /**
     * Trova un prodotto per ID con categorie caricate
     * @param id ID del prodotto
     * @return Optional contenente il prodotto completo se trovato
     */
    public Optional<Product> getProductDetailById(Long id) {
        log.info("Ricerca dettagli prodotto con ID: {}", id);
        Optional<Product> product = productRepository.findByIdWithCategories(id);
        if (product.isPresent()) {
            log.info("Dettagli prodotto trovati: {}", product.get().getName());
        } else {
            log.warn("Nessun prodotto attivo trovato con ID: {}", id);
        }
        return product;
    }
    
    /**
     * Trova un prodotto per slug con categorie caricate
     * @param slug Slug del prodotto
     * @return Optional contenente il prodotto completo se trovato
     */
    public Optional<Product> getProductDetailBySlug(String slug) {
        log.info("Ricerca dettagli prodotto per slug: {}", slug);
        Optional<Product> product = productRepository.findBySlugWithCategories(slug);
        if (product.isPresent()) {
            log.info("Dettagli prodotto trovati per slug: {}", product.get().getName());
        } else {
            log.warn("Nessun prodotto attivo trovato con slug: {}", slug);
        }
        return product;
    }
}
