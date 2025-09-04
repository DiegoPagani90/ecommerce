package com.example.backend.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AddToCartRequestDto;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CartItemResponseDto;
import com.example.backend.dto.CartResponseDto;
import com.example.backend.dto.UpdateCartItemRequestDto;
import com.example.backend.model.Cart;
import com.example.backend.model.CartItem;
import com.example.backend.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CartController {
    
    private static final String INTERNAL_SERVER_ERROR = "Errore interno del server";
    private final CartService cartService;
    
    /**
     * GET /api/cart/user/{userId}
     * Ottiene il carrello attivo dell'utente
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> getUserCart(@PathVariable Long userId) {
        log.info("Richiesta carrello per utente: {}", userId);
        
        try {
            Optional<Cart> cartOpt = cartService.getActiveCart(userId);
            
            if (cartOpt.isPresent()) {
                CartResponseDto cartDto = new CartResponseDto(cartOpt.get());
                return ResponseEntity.ok(ApiResponse.success("Carrello recuperato con successo", cartDto));
            } else {
                // Crea carrello vuoto se non esiste
                Cart newCart = cartService.getOrCreateActiveCart(userId);
                CartResponseDto cartDto = new CartResponseDto(newCart);
                return ResponseEntity.ok(ApiResponse.success("Nuovo carrello creato", cartDto));
            }
            
        } catch (Exception e) {
            log.error("Errore durante il recupero del carrello per utente {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * POST /api/cart/user/{userId}/add
     * Aggiunge un prodotto al carrello
     */
    @PostMapping("/user/{userId}/add")
    public ResponseEntity<ApiResponse<CartItemResponseDto>> addToCart(
            @PathVariable Long userId,
            @Valid @RequestBody AddToCartRequestDto request) {
        log.info("Aggiunta prodotto {} al carrello utente {} con quantità {}", 
                request.getProductId(), userId, request.getQuantity());
        
        try {
            CartItem cartItem = cartService.addProductToCart(
                    userId, 
                    request.getProductId(), 
                    request.getQuantity()
            );
            
            CartItemResponseDto itemDto = new CartItemResponseDto(cartItem);
            return ResponseEntity.ok(ApiResponse.success("Prodotto aggiunto al carrello", itemDto));
            
        } catch (IllegalArgumentException e) {
            log.warn("Richiesta non valida per aggiunta al carrello: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Errore durante l'aggiunta al carrello: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Errore interno durante l'aggiunta al carrello: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * PUT /api/cart/user/{userId}/update
     * Aggiorna la quantità di un prodotto nel carrello
     */
    @PutMapping("/user/{userId}/update")
    public ResponseEntity<ApiResponse<CartItemResponseDto>> updateCartItem(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateCartItemRequestDto request) {
        log.info("Aggiornamento quantità prodotto {} nel carrello utente {} a {}", 
                request.getProductId(), userId, request.getQuantity());
        
        try {
            CartItem cartItem = cartService.updateCartItemQuantity(
                    userId, 
                    request.getProductId(), 
                    request.getQuantity()
            );
            
            CartItemResponseDto itemDto = new CartItemResponseDto(cartItem);
            return ResponseEntity.ok(ApiResponse.success("Quantità aggiornata", itemDto));
            
        } catch (IllegalArgumentException e) {
            log.warn("Richiesta non valida per aggiornamento carrello: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Errore durante l'aggiornamento del carrello: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Errore interno durante l'aggiornamento del carrello: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * DELETE /api/cart/user/{userId}/remove/{productId}
     * Rimuove un prodotto dal carrello
     */
    @DeleteMapping("/user/{userId}/remove/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        log.info("Rimozione prodotto {} dal carrello utente {}", productId, userId);
        
        try {
            cartService.removeProductFromCart(userId, productId);
            return ResponseEntity.ok(ApiResponse.success("Prodotto rimosso dal carrello", null));
            
        } catch (RuntimeException e) {
            log.error("Errore durante la rimozione dal carrello: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Errore interno durante la rimozione dal carrello: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * DELETE /api/cart/user/{userId}/clear
     * Svuota completamente il carrello
     */
    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable Long userId) {
        log.info("Svuotamento carrello per utente: {}", userId);
        
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok(ApiResponse.success("Carrello svuotato", null));
            
        } catch (Exception e) {
            log.error("Errore durante lo svuotamento del carrello per utente {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/cart/user/{userId}/count
     * Ottiene il numero totale di item nel carrello
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(@PathVariable Long userId) {
        log.info("Richiesta conteggio item carrello per utente: {}", userId);
        
        try {
            int itemCount = cartService.getCartItemCount(userId);
            return ResponseEntity.ok(ApiResponse.success("Conteggio item recuperato", itemCount));
            
        } catch (Exception e) {
            log.error("Errore durante il conteggio item carrello per utente {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
}
