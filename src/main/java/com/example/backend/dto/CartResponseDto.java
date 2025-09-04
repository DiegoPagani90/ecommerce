package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.backend.model.Cart;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartResponseDto {
    
    private Long id;
    private Long userId;
    private String status;
    private List<CartItemResponseDto> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public CartResponseDto(Cart cart) {
        this.id = cart.getId();
        this.userId = cart.getUser().getId();
        this.status = cart.getStatus().toString();
        this.createdAt = cart.getCreatedAt();
        this.updatedAt = cart.getUpdatedAt();
        
        if (cart.getItems() != null) {
            this.items = cart.getItems().stream()
                    .map(CartItemResponseDto::new)
                    .toList();
            
            this.totalAmount = cart.getItems().stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            this.totalItems = cart.getItems().stream()
                    .mapToInt(item -> item.getQuantity())
                    .sum();
        } else {
            this.totalAmount = BigDecimal.ZERO;
            this.totalItems = 0;
        }
    }
}
