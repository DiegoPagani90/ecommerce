package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateOrderRequestDto {
    
    @NotNull(message = "L'ID dell'indirizzo di spedizione è obbligatorio")
    private Long shippingAddressId;
    
    private String notes;
}
