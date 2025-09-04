package com.example.backend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Trova ordini per utente
    List<Order> findByUserId(Long userId);
    
    // Trova ordini per utente ordinati per data
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Trova ordini per utente con paginazione
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Trova ordini per stato
    List<Order> findByStatus(Order.OrderStatus status);
    
    // Trova ordini per stato ordinati per data
    List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);
    
    // Trova ordini per utente e stato
    List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);
    
    // Query per ordini con items caricati
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
    
    // Query per ordini dell'utente con items
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);
    
    // Conta ordini per utente
    Long countByUserId(Long userId);
    
    // Somma totale speso da utente
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.user.id = :userId AND o.status != 'CANCELLED'")
    BigDecimal sumTotalAmountByUserId(@Param("userId") Long userId);
    
    // Trova ordini recenti
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :fromDate ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(@Param("fromDate") java.time.LocalDateTime fromDate);
}
