package com.example.backend.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CategoryRequestDto {
    
    @NotBlank(message = "Il nome della categoria è obbligatorio")
    @Size(min = 2, max = 100, message = "Il nome della categoria deve essere tra 2 e 100 caratteri")
    private String name;
    
    @Size(max = 500, message = "La descrizione non può superare i 500 caratteri")
    private String description;
}
