package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.model.Payment;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private static final String INTERNAL_SERVER_ERROR = "Errore interno del server";
    
    private final StripeService stripeService;
    private final PaymentRepository paymentRepository;
    
    @Value("${stripe.public.key}")
    private String stripePublicKey;
    
    /**
     * GET /api/payments/stripe/public-key
     * Restituisce la chiave pubblica di Stripe per il frontend
     */
    @GetMapping("/stripe/public-key")
    public ResponseEntity<ApiResponse<String>> getStripePublicKey() {
        log.info("Richiesta chiave pubblica Stripe");
        return ResponseEntity.ok(ApiResponse.success("Chiave pubblica Stripe", stripePublicKey));
    }
    
    /**
     * POST /api/payments/stripe/create-payment-intent
     * Crea un nuovo Payment Intent per un ordine
     */
    @PostMapping("/stripe/create-payment-intent")
    public ResponseEntity<ApiResponse<PaymentIntentResponseDto>> createPaymentIntent(
            @RequestBody PaymentIntentRequestDto request) {
        log.info("Richiesta creazione Payment Intent per ordine: {}", request.getOrderId());
        
        try {
            PaymentIntentResponseDto response = stripeService.createPaymentIntent(request);
            
            if (response.getClientSecret() != null) {
                return ResponseEntity.ok(ApiResponse.success("Payment Intent creato con successo", response));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            log.error("Errore durante la creazione del Payment Intent: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * POST /api/payments/stripe/confirm-payment
     * Conferma un Payment Intent
     */
    @PostMapping("/stripe/confirm-payment")
    public ResponseEntity<ApiResponse<PaymentIntentResponseDto>> confirmPayment(
            @RequestBody ConfirmPaymentRequestDto request) {
        log.info("Richiesta conferma Payment Intent: {}", request.getPaymentIntentId());
        
        try {
            PaymentIntentResponseDto response = stripeService.confirmPaymentIntent(
                    request.getPaymentIntentId(), 
                    request.getPaymentMethodId()
            );
            
            if ("succeeded".equals(response.getStatus())) {
                return ResponseEntity.ok(ApiResponse.success("Pagamento confermato con successo", response));
            } else {
                return ResponseEntity.ok(ApiResponse.success("Pagamento in elaborazione", response));
            }
            
        } catch (Exception e) {
            log.error("Errore durante la conferma del pagamento: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/payments/stripe/payment-intent/{paymentIntentId}/status
     * Recupera lo stato di un Payment Intent
     */
    @GetMapping("/stripe/payment-intent/{paymentIntentId}/status")
    public ResponseEntity<ApiResponse<PaymentIntentResponseDto>> getPaymentIntentStatus(
            @PathVariable String paymentIntentId) {
        log.info("Richiesta stato Payment Intent: {}", paymentIntentId);
        
        try {
            PaymentIntentResponseDto response = stripeService.getPaymentIntentStatus(paymentIntentId);
            
            return ResponseEntity.ok(ApiResponse.success("Stato recuperato con successo", response));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero dello stato del Payment Intent: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/payments/order/{orderId}
     * Ottiene tutti i pagamenti per un ordine
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getPaymentsByOrder(
            @PathVariable Long orderId) {
        log.info("Richiesta pagamenti per ordine: {}", orderId);
        
        try {
            List<Payment> payments = paymentRepository.findByOrderId(orderId);
            
            List<PaymentResponseDto> paymentDtos = payments.stream()
                    .map(PaymentResponseDto::new)
                    .toList();
            
            return ResponseEntity.ok(ApiResponse.success("Pagamenti recuperati con successo", paymentDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero dei pagamenti per ordine {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/payments/{paymentId}
     * Ottiene un pagamento specifico per ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentById(
            @PathVariable Long paymentId) {
        log.info("Richiesta pagamento con ID: {}", paymentId);
        
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            
            if (paymentOpt.isPresent()) {
                PaymentResponseDto paymentDto = new PaymentResponseDto(paymentOpt.get());
                return ResponseEntity.ok(ApiResponse.success("Pagamento trovato", paymentDto));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Errore durante il recupero del pagamento con ID {}: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * GET /api/payments/user/{userId}
     * Ottiene tutti i pagamenti per un utente
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getPaymentsByUser(
            @PathVariable Long userId) {
        log.info("Richiesta pagamenti per utente: {}", userId);
        
        try {
            List<Payment> payments = paymentRepository.findByUserId(userId);
            
            List<PaymentResponseDto> paymentDtos = payments.stream()
                    .map(PaymentResponseDto::new)
                    .toList();
            
            return ResponseEntity.ok(ApiResponse.success("Pagamenti utente recuperati con successo", paymentDtos));
            
        } catch (Exception e) {
            log.error("Errore durante il recupero dei pagamenti per utente {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(INTERNAL_SERVER_ERROR));
        }
    }
}
