package com.example.backend.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.Cart;
import com.example.backend.model.CartItem;
import com.example.backend.model.Product;
import com.example.backend.model.User;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {
    
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    /**
     * Ottiene il carrello attivo dell'utente, lo crea se non esiste
     */
    public Cart getOrCreateActiveCart(Long userId) {
        log.info("Ricerca carrello attivo per utente: {}", userId);
        
        Optional<Cart> existingCart = cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.OPEN);
        
        if (existingCart.isPresent()) {
            log.info("Carrello esistente trovato per utente: {}", userId);
            return existingCart.get();
        }
        
        // Crea nuovo carrello
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato: " + userId));
        
        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setStatus(Cart.CartStatus.OPEN);
        
        Cart savedCart = cartRepository.save(newCart);
        log.info("Nuovo carrello creato per utente: {} con ID: {}", userId, savedCart.getId());
        
        return savedCart;
    }
    
    /**
     * Aggiunge un prodotto al carrello
     */
    public CartItem addProductToCart(Long userId, Long productId, Integer quantity) {
        log.info("Aggiunta prodotto {} al carrello utente {} con quantità {}", productId, userId, quantity);
        
        // Validazioni
        if (quantity <= 0) {
            throw new IllegalArgumentException("La quantità deve essere maggiore di 0");
        }
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Prodotto non trovato: " + productId));
        
        if (!product.getIsActive()) {
            throw new RuntimeException("Prodotto non disponibile: " + productId);
        }
        
        if (product.getStockQty() < quantity) {
            throw new RuntimeException("Stock insufficiente per il prodotto: " + productId);
        }
        
        Cart cart = getOrCreateActiveCart(userId);
        
        // Controlla se il prodotto è già nel carrello
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Aggiorna quantità esistente
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            
            if (product.getStockQty() < newQuantity) {
                throw new RuntimeException("Stock insufficiente per il prodotto: " + productId);
            }
            
            cartItem.setQuantity(newQuantity);
            log.info("Quantità aggiornata per prodotto {} nel carrello: {}", productId, newQuantity);
        } else {
            // Crea nuovo item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());
            
            cart.getItems().add(cartItem);
            log.info("Nuovo item aggiunto al carrello per prodotto: {}", productId);
        }
        
        cartRepository.save(cart);
        return cartItem;
    }
    
    /**
     * Aggiorna la quantità di un prodotto nel carrello
     */
    public CartItem updateCartItemQuantity(Long userId, Long productId, Integer newQuantity) {
        log.info("Aggiornamento quantità prodotto {} nel carrello utente {} a {}", productId, userId, newQuantity);
        
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("La quantità deve essere maggiore di 0");
        }
        
        Cart cart = getOrCreateActiveCart(userId);
        
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Prodotto non trovato nel carrello: " + productId));
        
        Product product = cartItem.getProduct();
        if (product.getStockQty() < newQuantity) {
            throw new RuntimeException("Stock insufficiente per il prodotto: " + productId);
        }
        
        cartItem.setQuantity(newQuantity);
        cartRepository.save(cart);
        
        log.info("Quantità aggiornata per prodotto {} nel carrello: {}", productId, newQuantity);
        return cartItem;
    }
    
    /**
     * Rimuove un prodotto dal carrello
     */
    public void removeProductFromCart(Long userId, Long productId) {
        log.info("Rimozione prodotto {} dal carrello utente {}", productId, userId);
        
        Cart cart = getOrCreateActiveCart(userId);
        
        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        
        if (!removed) {
            throw new RuntimeException("Prodotto non trovato nel carrello: " + productId);
        }
        
        cartRepository.save(cart);
        log.info("Prodotto {} rimosso dal carrello utente {}", productId, userId);
    }
    
    /**
     * Svuota completamente il carrello
     */
    public void clearCart(Long userId) {
        log.info("Svuotamento carrello per utente: {}", userId);
        
        Cart cart = getOrCreateActiveCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        
        log.info("Carrello svuotato per utente: {}", userId);
    }
    
    /**
     * Calcola il totale del carrello
     */
    public BigDecimal calculateCartTotal(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Ottiene il carrello attivo dell'utente
     */
    @Transactional(readOnly = true)
    public Optional<Cart> getActiveCart(Long userId) {
        log.info("Recupero carrello attivo per utente: {}", userId);
        return cartRepository.findByUserIdAndStatusWithItems(userId, Cart.CartStatus.OPEN);
    }
    
    /**
     * Conta il numero totale di item nel carrello
     */
    @Transactional(readOnly = true)
    public int getCartItemCount(Long userId) {
        Optional<Cart> cart = getActiveCart(userId);
        if (cart.isPresent()) {
            return cart.get().getItems().stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();
        }
        return 0;
    }
    
    /**
     * Segna il carrello come checked out (utilizzato quando si crea l'ordine)
     */
    public void markCartAsCheckedOut(Long userId) {
        log.info("Marcatura carrello come checked out per utente: {}", userId);
        
        Optional<Cart> cartOpt = getActiveCart(userId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.setStatus(Cart.CartStatus.CHECKED_OUT);
            cartRepository.save(cart);
            
            log.info("Carrello {} marcato come checked out", cart.getId());
        }
    }
}
