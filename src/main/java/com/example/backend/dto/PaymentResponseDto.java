package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.backend.model.Payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    
    private Long id;
    private Long orderId;
    private String provider;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String stripePaymentIntentId;
    private String stripePaymentMethodId;
    private String receiptUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Costruttore che converte da Payment entity a DTO
    public PaymentResponseDto(Payment payment) {
        this.id = payment.getId();
        this.orderId = payment.getOrder().getId();
        this.provider = payment.getProvider();
        this.status = payment.getStatus().name();
        this.amount = payment.getAmount();
        this.currency = payment.getCurrency();
        this.stripePaymentIntentId = payment.getStripePaymentIntentId();
        this.stripePaymentMethodId = payment.getStripePaymentMethodId();
        this.receiptUrl = payment.getReceiptUrl();
        this.createdAt = payment.getCreatedAt();
        this.updatedAt = payment.getUpdatedAt();
    }
}
