package com.example.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateCartItemRequestDto {
    
    @NotNull(message = "L'ID del prodotto è obbligatorio")
    private Long productId;
    
    @NotNull(message = "La nuova quantità è obbligatoria")
    @Min(value = 1, message = "La quantità deve essere almeno 1")
    private Integer quantity;
}
