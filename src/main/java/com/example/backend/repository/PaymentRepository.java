package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Trova payment per Stripe Payment Intent ID
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    // Trova tutti i pagamenti per un ordine
    List<Payment> findByOrderId(Long orderId);
    
    // Trova pagamenti per stato
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    // Trova pagamenti per utente
    @Query("SELECT p FROM Payment p WHERE p.order.user.id = :userId")
    List<Payment> findByUserId(@Param("userId") Long userId);
    
    // Trova pagamenti riusciti per un ordine
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.status = 'SUCCEEDED'")
    List<Payment> findSuccessfulPaymentsByOrderId(@Param("orderId") Long orderId);
}
