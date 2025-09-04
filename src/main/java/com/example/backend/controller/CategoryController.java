package com.example.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CategoryRequestDto;
import com.example.backend.dto.CategoryResponseDto;
import com.example.backend.dto.ProductResponseDto;
import com.example.backend.model.Category;
import com.example.backend.model.Product;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CategoryController {
    
    private static final String INTERNAL_SERVER_ERROR = "Errore interno del server";
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    
    /**
     * GET /api/categories
     * Ottiene tutte le categorie
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories() {
        log.info("Richiesta tutte le categorie");
        
        try {
            List<Category> categories = categoryRepository.findByActiveTrue();
            List<CategoryResponseDto> categoryDtos = categories.stream()
                    .map(CategoryResponseDto::new)
                    .toList();
            
            return ResponseEntity.ok(ApiResponse.success("Categorie recuperate con successo", categoryDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero delle categorie: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/categories/{id}
     * Ottiene una categoria per ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable Long id) {
        log.info("Richiesta categoria con ID: {}", id);
        
        try {
            Optional<Category> categoryOpt = categoryRepository.findByIdAndActiveTrue(id);
            
            if (categoryOpt.isPresent()) {
                CategoryResponseDto categoryDto = new CategoryResponseDto(categoryOpt.get());
                return ResponseEntity.ok(ApiResponse.success("Categoria trovata", categoryDto));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Errore durante il recupero della categoria {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/categories/{id}/products
     * Ottiene tutti i prodotti di una categoria
     */
    @GetMapping("/{id}/products")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getCategoryProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Richiesta prodotti per categoria {} (page: {}, size: {})", id, page, size);
        
        try {
            Optional<Category> categoryOpt = categoryRepository.findByIdAndActiveTrue(id);
            
            if (categoryOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> productsPage = productRepository.findByCategoryIdAndActiveTrue(id, pageable);
            
            List<ProductResponseDto> productDtos = productsPage.getContent().stream()
                    .map(ProductResponseDto::new)
                    .toList();
            
            return ResponseEntity.ok(ApiResponse.success("Prodotti recuperati con successo", productDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero dei prodotti per categoria {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * POST /api/categories
     * Crea una nuova categoria
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(@Valid @RequestBody CategoryRequestDto request) {
        log.info("Creazione nuova categoria: {}", request.getName());
        
        try {
            // Verifica se esiste già una categoria con lo stesso nome
            if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Esiste già una categoria con questo nome"));
            }
            
            Category category = new Category();
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            category.setActive(true);
            
            Category savedCategory = categoryRepository.save(category);
            CategoryResponseDto categoryDto = new CategoryResponseDto(savedCategory);
            
            return ResponseEntity.ok(ApiResponse.success("Categoria creata con successo", categoryDto));
            
        } catch (Exception e) {
            log.error("Errore durante la creazione della categoria: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * PUT /api/categories/{id}
     * Aggiorna una categoria
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDto request) {
        log.info("Aggiornamento categoria {}: {}", id, request.getName());
        
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            
            if (categoryOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Category category = categoryOpt.get();
            
            // Verifica se il nuovo nome non è già utilizzato da un'altra categoria
            if (!category.getName().equalsIgnoreCase(request.getName()) && 
                categoryRepository.existsByNameIgnoreCase(request.getName())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Esiste già una categoria con questo nome"));
            }
            
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            
            Category savedCategory = categoryRepository.save(category);
            CategoryResponseDto categoryDto = new CategoryResponseDto(savedCategory);
            
            return ResponseEntity.ok(ApiResponse.success("Categoria aggiornata con successo", categoryDto));
            
        } catch (Exception e) {
            log.error("Errore durante l'aggiornamento della categoria {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * DELETE /api/categories/{id}
     * Elimina (disattiva) una categoria
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        log.info("Eliminazione categoria: {}", id);
        
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            
            if (categoryOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Category category = categoryOpt.get();
            
            // Verifica se ci sono prodotti attivi in questa categoria
            long activeProductsCount = productRepository.countByCategoryIdAndActiveTrue(id);
            if (activeProductsCount > 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Non è possibile eliminare una categoria che contiene prodotti attivi"));
            }
            
            // Disattiva la categoria invece di eliminarla fisicamente
            category.setActive(false);
            categoryRepository.save(category);
            
            return ResponseEntity.ok(ApiResponse.success("Categoria eliminata con successo", null));
            
        } catch (Exception e) {
            log.error("Errore durante l'eliminazione della categoria {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/categories/search
     * Cerca categorie per nome
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> searchCategories(@RequestParam String query) {
        log.info("Ricerca categorie con query: {}", query);
        
        try {
            List<Category> categories = categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue(query);
            List<CategoryResponseDto> categoryDtos = categories.stream()
                    .map(CategoryResponseDto::new)
                    .toList();
            
            return ResponseEntity.ok(ApiResponse.success("Ricerca completata", categoryDtos));
            
        } catch (Exception e) {
            log.error("Errore durante la ricerca delle categorie: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/categories/{id}/statistics
     * Ottiene le statistiche di una categoria
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<ApiResponse<CategoryStatistics>> getCategoryStatistics(@PathVariable Long id) {
        log.info("Richiesta statistiche per categoria: {}", id);
        
        try {
            Optional<Category> categoryOpt = categoryRepository.findByIdAndActiveTrue(id);
            
            if (categoryOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            long totalProducts = productRepository.countByCategoryId(id);
            long activeProducts = productRepository.countByCategoryIdAndActiveTrue(id);
            
            CategoryStatistics statistics = new CategoryStatistics(
                    id,
                    categoryOpt.get().getName(),
                    totalProducts,
                    activeProducts
            );
            
            return ResponseEntity.ok(ApiResponse.success("Statistiche recuperate", statistics));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero delle statistiche per categoria {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * Record per le statistiche della categoria
     */
    public record CategoryStatistics(
            Long categoryId,
            String categoryName,
            long totalProducts,
            long activeProducts
    ) {}
}
