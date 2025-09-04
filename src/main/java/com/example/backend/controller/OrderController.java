package com.example.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.CreateOrderRequestDto;
import com.example.backend.dto.OrderResponseDto;
import com.example.backend.model.Order;
import com.example.backend.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderController {
    
    private static final String INTERNAL_SERVER_ERROR = "Errore interno del server";
    private final OrderService orderService;
    
    /**
     * POST /api/orders/user/{userId}/create
     * Crea un nuovo ordine dal carrello dell'utente
     */
    @PostMapping("/user/{userId}/create")
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @PathVariable Long userId,
            @Valid @RequestBody CreateOrderRequestDto request) {
        log.info("Creazione ordine per utente: {}", userId);
        
        try {
            Order order = orderService.createOrderFromCart(
                    userId,
                    request.getShippingAddressId(),
                    request.getNotes()
            );
            
            OrderResponseDto orderDto = new OrderResponseDto(order);
            return ResponseEntity.ok(ApiResponse.success("Ordine creato con successo", orderDto));
            
        } catch (RuntimeException e) {
            log.error("Errore durante la creazione dell'ordine: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Errore interno durante la creazione dell'ordine: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/orders/{orderId}
     * Ottiene i dettagli di un ordine
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(@PathVariable Long orderId) {
        log.info("Richiesta dettagli ordine: {}", orderId);
        
        try {
            Optional<Order> orderOpt = orderService.getOrderWithDetails(orderId);
            
            if (orderOpt.isPresent()) {
                OrderResponseDto orderDto = new OrderResponseDto(orderOpt.get());
                return ResponseEntity.ok(ApiResponse.success("Ordine trovato", orderDto));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Errore durante il recupero dell'ordine {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/orders/user/{userId}
     * Ottiene tutti gli ordini di un utente
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Richiesta ordini per utente: {} (page: {}, size: {})", userId, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> ordersPage = orderService.getUserOrdersPaginated(userId, pageable);
            
            List<OrderResponseDto> orderDtos = ordersPage.getContent().stream()
                    .map(OrderResponseDto::new)
                    .toList();
            
            return ResponseEntity.ok(ApiResponse.success("Ordini recuperati con successo", orderDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero degli ordini per utente {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/orders/status/{status}
     * Ottiene tutti gli ordini con un determinato stato
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersByStatus(@PathVariable String status) {
        log.info("Richiesta ordini con stato: {}", status);
        
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(orderStatus);
            
            List<OrderResponseDto> orderDtos = orders.stream()
                    .map(OrderResponseDto::new)
                    .toList();
            
            return ResponseEntity.ok(ApiResponse.success("Ordini recuperati con successo", orderDtos));
            
        } catch (IllegalArgumentException e) {
            log.warn("Stato ordine non valido: {}", status);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Stato ordine non valido: " + status));
        } catch (Exception e) {
            log.error("Errore durante il recupero degli ordini per stato {}: {}", status, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * PUT /api/orders/{orderId}/status/{status}
     * Aggiorna lo stato di un ordine
     */
    @PutMapping("/{orderId}/status/{status}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrderStatus(
            @PathVariable Long orderId,
            @PathVariable String status,
            @RequestParam(required = false) String note) {
        log.info("Aggiornamento stato ordine {} a: {}", orderId, status);
        
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            Order updatedOrder = orderService.updateOrderStatus(orderId, orderStatus, note);
            
            OrderResponseDto orderDto = new OrderResponseDto(updatedOrder);
            return ResponseEntity.ok(ApiResponse.success("Stato ordine aggiornato", orderDto));
            
        } catch (IllegalArgumentException e) {
            log.warn("Stato ordine non valido: {}", status);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Stato ordine non valido: " + status));
        } catch (RuntimeException e) {
            log.error("Errore durante l'aggiornamento dello stato dell'ordine: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Errore interno durante l'aggiornamento dello stato dell'ordine: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * PUT /api/orders/{orderId}/cancel
     * Cancella un ordine
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponseDto>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        log.info("Cancellazione ordine: {}", orderId);
        
        try {
            Order cancelledOrder = orderService.cancelOrder(orderId, reason != null ? reason : "Cancellato dall'utente");
            
            OrderResponseDto orderDto = new OrderResponseDto(cancelledOrder);
            return ResponseEntity.ok(ApiResponse.success("Ordine cancellato", orderDto));
            
        } catch (RuntimeException e) {
            log.error("Errore durante la cancellazione dell'ordine: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Errore interno durante la cancellazione dell'ordine: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * PUT /api/orders/{orderId}/confirm
     * Conferma un ordine
     */
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<OrderResponseDto>> confirmOrder(@PathVariable Long orderId) {
        log.info("Conferma ordine: {}", orderId);
        
        try {
            Order confirmedOrder = orderService.confirmOrder(orderId);
            
            OrderResponseDto orderDto = new OrderResponseDto(confirmedOrder);
            return ResponseEntity.ok(ApiResponse.success("Ordine confermato", orderDto));
            
        } catch (RuntimeException e) {
            log.error("Errore durante la conferma dell'ordine: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Errore interno durante la conferma dell'ordine: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * PUT /api/orders/{orderId}/ship
     * Marca un ordine come spedito
     */
    @PutMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponse<OrderResponseDto>> shipOrder(
            @PathVariable Long orderId,
            @RequestParam String trackingNumber) {
        log.info("Spedizione ordine: {} con tracking: {}", orderId, trackingNumber);
        
        try {
            Order shippedOrder = orderService.shipOrder(orderId, trackingNumber);
            
            OrderResponseDto orderDto = new OrderResponseDto(shippedOrder);
            return ResponseEntity.ok(ApiResponse.success("Ordine spedito", orderDto));
            
        } catch (RuntimeException e) {
            log.error("Errore durante la spedizione dell'ordine: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Errore interno durante la spedizione dell'ordine: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * PUT /api/orders/{orderId}/deliver
     * Marca un ordine come consegnato
     */
    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<ApiResponse<OrderResponseDto>> deliverOrder(@PathVariable Long orderId) {
        log.info("Consegna ordine: {}", orderId);
        
        try {
            Order deliveredOrder = orderService.deliverOrder(orderId);
            
            OrderResponseDto orderDto = new OrderResponseDto(deliveredOrder);
            return ResponseEntity.ok(ApiResponse.success("Ordine consegnato", orderDto));
            
        } catch (RuntimeException e) {
            log.error("Errore durante la consegna dell'ordine: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Errore interno durante la consegna dell'ordine: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/orders/user/{userId}/statistics
     * Ottiene le statistiche degli ordini di un utente
     */
    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<ApiResponse<OrderService.OrderStatistics>> getUserOrderStatistics(@PathVariable Long userId) {
        log.info("Richiesta statistiche ordini per utente: {}", userId);
        
        try {
            OrderService.OrderStatistics statistics = orderService.getUserOrderStatistics(userId);
            return ResponseEntity.ok(ApiResponse.success("Statistiche recuperate", statistics));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero delle statistiche per utente {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
}
