package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.backend.model.Order;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderResponseDto {
    
    private Long id;
    private Long userId;
    private String status;
    private BigDecimal subtotalAmount;
    private BigDecimal shippingAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String trackingNumber;
    private String notes;
    private List<OrderItemResponseDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public OrderResponseDto(Order order) {
        this.id = order.getId();
        this.userId = order.getUser().getId();
        this.status = order.getStatus().toString();
        this.subtotalAmount = order.getSubtotalAmount();
        this.shippingAmount = order.getShippingAmount();
        this.taxAmount = order.getTaxAmount();
        this.discountAmount = order.getDiscountAmount();
        this.totalAmount = order.getTotalAmount();
        this.currency = order.getCurrency();
        this.trackingNumber = order.getTrackingNumber();
        this.notes = order.getNotes();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
        
        if (order.getItems() != null) {
            this.items = order.getItems().stream()
                    .map(OrderItemResponseDto::new)
                    .toList();
        }
    }
}
