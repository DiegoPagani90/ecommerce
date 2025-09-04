package com.example.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.ProductDetailDto;
import com.example.backend.dto.ProductResponseDto;
import com.example.backend.model.Product;
import com.example.backend.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Per permettere richieste da frontend in sviluppo
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * GET /api/products
     * Ottiene tutti i prodotti attivi dal database
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getAllProducts() {
        log.info("Richiesta GET /api/products - Recupero di tutti i prodotti attivi");
        
        try {
            List<Product> products = productService.getActiveProducts();
            
            // Converte i prodotti in DTO per evitare problemi di serializzazione JSON
            List<ProductResponseDto> productDtos = products.stream()
                    .map(ProductResponseDto::new)
                    .toList();
            
            log.info("Restituiti {} prodotti attivi", productDtos.size());
            return ResponseEntity.ok(ApiResponse.success("Prodotti recuperati con successo", productDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero dei prodotti: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero dei prodotti"));
        }
    }
    
    /**
     * GET /api/products/with-categories
     * Ottiene tutti i prodotti attivi con le categorie caricate
     */
    @GetMapping("/with-categories")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getAllProductsWithCategories() {
        log.info("Richiesta GET /api/products/with-categories - Recupero prodotti con categorie");
        
        try {
            List<Product> products = productService.getActiveProductsWithCategories();
            
            List<ProductResponseDto> productDtos = products.stream()
                    .map(ProductResponseDto::new)
                    .toList();
            
            log.info("Restituiti {} prodotti con categorie", productDtos.size());
            return ResponseEntity.ok(ApiResponse.success("Prodotti con categorie recuperati con successo", productDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero dei prodotti con categorie: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero dei prodotti con categorie"));
        }
    }
    
    /**
     * GET /api/products/{id}
     * Ottiene un prodotto specifico per ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductById(@PathVariable Long id) {
        log.info("Richiesta GET /api/products/{} - Recupero prodotto per ID", id);
        
        try {
            Optional<Product> productOpt = productService.getProductById(id);
            
            if (productOpt.isPresent()) {
                ProductResponseDto productDto = new ProductResponseDto(productOpt.get());
                log.info("Prodotto trovato: {}", productDto.getName());
                return ResponseEntity.ok(ApiResponse.success("Prodotto trovato", productDto));
            } else {
                log.warn("Prodotto con ID {} non trovato", id);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Errore durante il recupero del prodotto con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero del prodotto"));
        }
    }
    
    /**
     * GET /api/products/slug/{slug}
     * Ottiene un prodotto per slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductBySlug(@PathVariable String slug) {
        log.info("Richiesta GET /api/products/slug/{} - Recupero prodotto per slug", slug);
        
        try {
            Optional<Product> productOpt = productService.getProductBySlug(slug);
            
            if (productOpt.isPresent()) {
                ProductResponseDto productDto = new ProductResponseDto(productOpt.get());
                log.info("Prodotto trovato per slug: {}", productDto.getName());
                return ResponseEntity.ok(ApiResponse.success("Prodotto trovato", productDto));
            } else {
                log.warn("Prodotto con slug {} non trovato", slug);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Errore durante il recupero del prodotto con slug {}: {}", slug, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero del prodotto"));
        }
    }
    
    /**
     * GET /api/products/search/name?q=termine
     * Cerca prodotti per nome
     */
    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> searchProductsByName(
            @RequestParam(value = "q", required = false, defaultValue = "") String query) {
        log.info("Richiesta GET /api/products/search/name?q={} - Ricerca prodotti per nome", query);
        
        try {
            if (query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Il parametro di ricerca 'q' non può essere vuoto"));
            }
            
            List<Product> products = productService.getProductsByName(query.trim());
            
            List<ProductResponseDto> productDtos = products.stream()
                    .map(ProductResponseDto::new)
                    .toList();
            
            log.info("Trovati {} prodotti per nome '{}'", productDtos.size(), query);
            return ResponseEntity.ok(ApiResponse.success(
                    String.format("Trovati %d prodotti per '%s'", productDtos.size(), query), 
                    productDtos));
            
        } catch (Exception e) {
            log.error("Errore durante la ricerca prodotti per nome '{}': {}", query, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante la ricerca"));
        }
    }
    
    /**
     * GET /api/products/search?q=termine
     * Ricerca avanzata prodotti (nome, descrizione, SKU)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> searchProducts(
            @RequestParam(value = "q", required = false, defaultValue = "") String query) {
        log.info("Richiesta GET /api/products/search?q={} - Ricerca avanzata prodotti", query);
        
        try {
            if (query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Il parametro di ricerca 'q' non può essere vuoto"));
            }
            
            List<Product> products = productService.searchProducts(query.trim());
            
            List<ProductResponseDto> productDtos = products.stream()
                    .map(ProductResponseDto::new)
                    .toList();
            
            log.info("Trovati {} prodotti per ricerca '{}'", productDtos.size(), query);
            return ResponseEntity.ok(ApiResponse.success(
                    String.format("Trovati %d prodotti per '%s'", productDtos.size(), query), 
                    productDtos));
            
        } catch (Exception e) {
            log.error("Errore durante la ricerca avanzata prodotti per '{}': {}", query, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante la ricerca"));
        }
    }
    
    /**
     * GET /api/products/category/{categoryId}
     * Ottiene prodotti per categoria
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getProductsByCategory(@PathVariable Long categoryId) {
        log.info("Richiesta GET /api/products/category/{} - Recupero prodotti per categoria", categoryId);
        
        try {
            List<Product> products = productService.getProductsByCategory(categoryId);
            
            List<ProductResponseDto> productDtos = products.stream()
                    .map(ProductResponseDto::new)
                    .toList();
            
            log.info("Trovati {} prodotti nella categoria {}", productDtos.size(), categoryId);
            return ResponseEntity.ok(ApiResponse.success(
                    String.format("Trovati %d prodotti nella categoria", productDtos.size()), 
                    productDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero prodotti per categoria {}: {}", categoryId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero dei prodotti"));
        }
    }
    
    /**
     * GET /api/products/in-stock?min=0
     * Ottiene prodotti con stock disponibile
     */
    @GetMapping("/in-stock")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getProductsInStock(
            @RequestParam(value = "min", required = false, defaultValue = "0") Integer minStock) {
        log.info("Richiesta GET /api/products/in-stock?min={} - Recupero prodotti con stock", minStock);
        
        try {
            List<Product> products = productService.getProductsWithStock(minStock);
            
            List<ProductResponseDto> productDtos = products.stream()
                    .map(ProductResponseDto::new)
                    .toList();
            
            log.info("Trovati {} prodotti con stock > {}", productDtos.size(), minStock);
            return ResponseEntity.ok(ApiResponse.success(
                    String.format("Trovati %d prodotti con stock disponibile", productDtos.size()), 
                    productDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero prodotti con stock > {}: {}", minStock, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero dei prodotti"));
        }
    }
    
    /**
     * GET /api/products/count
     * Ottiene il conteggio dei prodotti attivi
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countActiveProducts() {
        log.info("Richiesta GET /api/products/count - Conteggio prodotti attivi");
        
        try {
            Long count = productService.countActiveProducts();
            log.info("Conteggio prodotti attivi: {}", count);
            return ResponseEntity.ok(ApiResponse.success("Conteggio completato", count));
            
        } catch (Exception e) {
            log.error("Errore durante il conteggio dei prodotti attivi: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il conteggio"));
        }
    }
    
    /**
     * GET /api/products/{id}/details
     * Ottiene i dettagli completi di un prodotto per ID (per pagina prodotto del frontend)
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductDetails(@PathVariable Long id) {
        log.info("Richiesta GET /api/products/{}/details - Recupero dettagli prodotto", id);
        
        try {
            Optional<Product> productOpt = productService.getProductDetailById(id);
            
            if (productOpt.isPresent()) {
                ProductDetailDto productDetailDto = new ProductDetailDto(productOpt.get());
                log.info("Dettagli prodotto trovati: {}", productDetailDto.getName());
                return ResponseEntity.ok(ApiResponse.success("Dettagli prodotto recuperati con successo", productDetailDto));
            } else {
                log.warn("Prodotto con ID {} non trovato o non attivo", id);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Errore durante il recupero dei dettagli del prodotto con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero dei dettagli del prodotto"));
        }
    }
    
    /**
     * GET /api/products/slug/{slug}/details
     * Ottiene i dettagli completi di un prodotto per slug (per URL SEO-friendly)
     */
    @GetMapping("/slug/{slug}/details")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductDetailsBySlug(@PathVariable String slug) {
        log.info("Richiesta GET /api/products/slug/{}/details - Recupero dettagli prodotto per slug", slug);
        
        try {
            Optional<Product> productOpt = productService.getProductDetailBySlug(slug);
            
            if (productOpt.isPresent()) {
                ProductDetailDto productDetailDto = new ProductDetailDto(productOpt.get());
                log.info("Dettagli prodotto trovati per slug: {}", productDetailDto.getName());
                return ResponseEntity.ok(ApiResponse.success("Dettagli prodotto recuperati con successo", productDetailDto));
            } else {
                log.warn("Prodotto con slug {} non trovato o non attivo", slug);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Errore durante il recupero dei dettagli del prodotto con slug {}: {}", slug, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Errore interno del server durante il recupero dei dettagli del prodotto"));
        }
    }
}
