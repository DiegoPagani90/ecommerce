package com.example.backend.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentRequestDto {
    
    private BigDecimal amount;
    private String currency = "EUR";
    private Long orderId;
    private String description;
    private String receiptEmail;
    
    // Metodo per convertire l'importo in centesimi (formato richiesto da Stripe)
    public Long getAmountInCents() {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }
}
