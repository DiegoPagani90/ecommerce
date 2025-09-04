package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    
    @NotBlank(message = "L'username è obbligatorio")
    private String username;
    
    @NotBlank(message = "La password è obbligatoria")
    private String password;
}
