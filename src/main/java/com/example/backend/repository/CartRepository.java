package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.model.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * Trova il carrello per utente e stato
     */
    Optional<Cart> findByUserIdAndStatus(Long userId, Cart.CartStatus status);
    
    /**
     * Trova tutti i carrelli di un utente
     */
    List<Cart> findByUserId(Long userId);
    
    /**
     * Trova carrello attivo con items caricati
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product " +
           "WHERE c.user.id = :userId AND c.status = :status")
    Optional<Cart> findByUserIdAndStatusWithItems(@Param("userId") Long userId, 
                                                   @Param("status") Cart.CartStatus status);
    
    /**
     * Trova tutti i carrelli attivi con items
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product " +
           "WHERE c.status = 'OPEN'")
    List<Cart> findAllActiveCartsWithItems();
    
    /**
     * Trova carrelli abbandonati (non aggiornati da più di X giorni)
     */
    @Query("SELECT c FROM Cart c WHERE c.status = 'OPEN' AND c.updatedAt < :cutoffDate")
    List<Cart> findAbandonedCarts(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
    
    /**
     * Conta il numero di carrelli attivi
     */
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.status = 'OPEN'")
    Long countActiveCarts();
    
    /**
     * Elimina carrelli vecchi per cleanup
     */
    @Query("DELETE FROM Cart c WHERE c.status = 'CHECKED_OUT' AND c.updatedAt < :cutoffDate")
    void deleteOldCheckedOutCarts(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
