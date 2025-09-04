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
@Transactional
public class StockService {
    
    private final ProductRepository productRepository;
    
    /**
     * Verifica se c'è abbastanza stock per un prodotto
     */
    public boolean isStockAvailable(Long productId, int quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            log.warn("Prodotto non trovato: {}", productId);
            return false;
        }
        
        Product product = productOpt.get();
        boolean available = product.getStockQty() >= quantity;
        
        log.debug("Verifica stock per prodotto {}: richiesti {}, disponibili {}, risultato: {}", 
                productId, quantity, product.getStockQty(), available);
        
        return available;
    }
    
    /**
     * Riduce lo stock di un prodotto
     */
    public boolean reduceStock(Long productId, int quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            log.error("Impossibile ridurre stock: prodotto non trovato {}", productId);
            return false;
        }
        
        Product product = productOpt.get();
        
        if (product.getStockQty() < quantity) {
            log.error("Stock insufficiente per prodotto {}: richiesti {}, disponibili {}", 
                    productId, quantity, product.getStockQty());
            return false;
        }
        
        int newStock = product.getStockQty() - quantity;
        product.setStockQty(newStock);
        productRepository.save(product);
        
        log.info("Stock ridotto per prodotto {}: da {} a {} (riduzione: {})", 
                productId, product.getStockQty() + quantity, newStock, quantity);
        
        return true;
    }
    
    /**
     * Aumenta lo stock di un prodotto (per cancellazioni o restituzioni)
     */
    public void increaseStock(Long productId, int quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            log.error("Impossibile aumentare stock: prodotto non trovato {}", productId);
            return;
        }
        
        Product product = productOpt.get();
        int newStock = product.getStockQty() + quantity;
        product.setStockQty(newStock);
        productRepository.save(product);
        
        log.info("Stock aumentato per prodotto {}: da {} a {} (aumento: {})", 
                productId, product.getStockQty() - quantity, newStock, quantity);
    }
    
    /**
     * Aggiorna lo stock di un prodotto
     */
    public void updateStock(Long productId, int newStock) {
        Optional<Product> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            log.error("Impossibile aggiornare stock: prodotto non trovato {}", productId);
            return;
        }
        
        Product product = productOpt.get();
        int oldStock = product.getStockQty();
        product.setStockQty(newStock);
        productRepository.save(product);
        
        log.info("Stock aggiornato per prodotto {}: da {} a {}", 
                productId, oldStock, newStock);
    }
    
    /**
     * Ottiene tutti i prodotti con stock basso (sotto una soglia)
     */
    public List<Product> getLowStockProducts(int threshold) {
        List<Product> lowStockProducts = productRepository.findByStockQtyLessThanEqualAndIsActiveTrue(threshold);
        
        log.info("Trovati {} prodotti con stock basso (soglia: {})", 
                lowStockProducts.size(), threshold);
        
        return lowStockProducts;
    }
    
    /**
     * Ottiene tutti i prodotti esauriti
     */
    public List<Product> getOutOfStockProducts() {
        return getLowStockProducts(0);
    }
    
    /**
     * Verifica se un prodotto è esaurito
     */
    public boolean isOutOfStock(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            return true; // Consideriamo un prodotto non trovato come esaurito
        }
        
        return productOpt.get().getStockQty() <= 0;
    }
    
    /**
     * Riserva temporaneamente stock per un ordine (per evitare overselling)
     * Nota: In un'implementazione reale, questo richiederebbe una tabella 
     * separata per le prenotazioni di stock
     */
    public boolean reserveStock(Long productId, int quantity) {
        // Per ora, usiamo semplicemente la verifica di disponibilità
        // In futuro, si potrebbe implementare un sistema di prenotazioni
        return isStockAvailable(productId, quantity);
    }
    
    /**
     * Rilascia la prenotazione di stock
     */
    public void releaseStockReservation(Long productId, int quantity) {
        // Per ora, non facciamo nulla
        // In futuro, si implementerebbe il rilascio delle prenotazioni
        log.debug("Rilascio prenotazione stock per prodotto {}: quantità {}", 
                productId, quantity);
    }
}
