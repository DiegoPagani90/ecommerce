package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentResponseDto {
    
    private String clientSecret;
    private String paymentIntentId;
    private String status;
    private String message;
    
    public static PaymentIntentResponseDto success(String clientSecret, String paymentIntentId, String status) {
        return new PaymentIntentResponseDto(clientSecret, paymentIntentId, status, "Payment Intent creato con successo");
    }
    
    public static PaymentIntentResponseDto error(String message) {
        return new PaymentIntentResponseDto(null, null, "failed", message);
    }
}
