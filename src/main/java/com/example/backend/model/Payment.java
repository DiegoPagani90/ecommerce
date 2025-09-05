package com.example.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @NotBlank(message = "Il provider è obbligatorio")
    @Size(max = 50, message = "Il provider non può superare i 50 caratteri")
    @Column(nullable = false)
    private String provider = "stripe";
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.REQUIRES_PAYMENT_METHOD;
    
    @NotNull(message = "L'importo è obbligatorio")
    @DecimalMin(value = "0.01", message = "L'importo deve essere maggiore di 0")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Size(max = 3, message = "La valuta deve essere di 3 caratteri")
    @Column(nullable = false)
    private String currency = "EUR";
    
    @NotBlank(message = "L'ID del payment intent Stripe è obbligatorio")
    @Size(max = 100, message = "L'ID del payment intent non può superare i 100 caratteri")
    @Column(name = "stripe_payment_intent_id", unique = true, nullable = false)
    private String stripePaymentIntentId;
    
    @Size(max = 100, message = "L'ID del payment method non può superare i 100 caratteri")
    @Column(name = "stripe_payment_method_id")
    private String stripePaymentMethodId;
    
    @Size(max = 500, message = "L'URL della ricevuta non può superare i 500 caratteri")
    @Column(name = "receipt_url")
    private String receiptUrl;
    
    @Column(name = "raw_response", columnDefinition = "JSON")
    private String rawResponse;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Custom equals method using only id to avoid circular references
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return java.util.Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", status=" + status +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", stripePaymentIntentId='" + stripePaymentIntentId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    
    public enum PaymentStatus {
        REQUIRES_PAYMENT_METHOD,
        REQUIRES_CONFIRMATION,
        REQUIRES_ACTION,
        PROCESSING,
        SUCCEEDED,
        CANCELED,
        FAILED,
        REFUNDED
    }
}
