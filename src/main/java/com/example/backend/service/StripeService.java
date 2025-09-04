package com.example.backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.PaymentIntentRequestDto;
import com.example.backend.dto.PaymentIntentResponseDto;
import com.example.backend.model.Order;
import com.example.backend.model.Payment;
import com.example.backend.model.User;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StripeService {
    
    private static final String INTERNAL_SERVER_ERROR = "Errore interno del server";
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    
    /**
     * Crea un Payment Intent per un ordine
     */
    public PaymentIntentResponseDto createPaymentIntent(PaymentIntentRequestDto request) {
        try {
            log.info("Creazione Payment Intent per ordine ID: {}", request.getOrderId());
            
            // Verifica che l'ordine esista
            Optional<Order> orderOpt = orderRepository.findById(request.getOrderId());
            if (orderOpt.isEmpty()) {
                log.error("Ordine con ID {} non trovato", request.getOrderId());
                return PaymentIntentResponseDto.error("Ordine non trovato");
            }
            
            Order order = orderOpt.get();
            User user = order.getUser();
            
            // Crea o recupera il customer Stripe
            String customerId = getOrCreateStripeCustomer(user);
            
            // Crea il Payment Intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmountInCents())
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setCustomer(customerId)
                    .setDescription(request.getDescription() != null ? 
                            request.getDescription() : 
                            "Pagamento per ordine #" + order.getId())
                    .setReceiptEmail(request.getReceiptEmail() != null ? 
                            request.getReceiptEmail() : 
                            user.getEmail())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .putMetadata("order_id", order.getId().toString())
                    .putMetadata("user_id", user.getId().toString())
                    .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Salva il payment nel database
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setProvider("stripe");
            payment.setStatus(Payment.PaymentStatus.REQUIRES_PAYMENT_METHOD);
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency());
            payment.setStripePaymentIntentId(paymentIntent.getId());
            payment.setRawResponse(paymentIntent.toJson());
            
            paymentRepository.save(payment);
            
            log.info("Payment Intent creato con successo: {}", paymentIntent.getId());
            
            return PaymentIntentResponseDto.success(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    paymentIntent.getStatus()
            );
            
        } catch (StripeException e) {
            log.error("Errore Stripe durante la creazione del Payment Intent: {}", e.getMessage(), e);
            return PaymentIntentResponseDto.error("Errore durante la creazione del pagamento: " + e.getUserMessage());
        } catch (Exception e) {
            log.error("Errore generico durante la creazione del Payment Intent: {}", e.getMessage(), e);
            return PaymentIntentResponseDto.error(INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Conferma un Payment Intent
     */
    public PaymentIntentResponseDto confirmPaymentIntent(String paymentIntentId, String paymentMethodId) {
        try {
            log.info("Conferma Payment Intent: {}", paymentIntentId);
            
            PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                    .setPaymentMethod(paymentMethodId)
                    .setReturnUrl("http://localhost:3000/payment/success") // URL del frontend
                    .build();
            
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            paymentIntent = paymentIntent.confirm(params);
            
            // Aggiorna il payment nel database
            updatePaymentFromStripe(paymentIntent);
            
            log.info("Payment Intent confermato: {}", paymentIntent.getStatus());
            
            return PaymentIntentResponseDto.success(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    paymentIntent.getStatus()
            );
            
        } catch (StripeException e) {
            log.error("Errore Stripe durante la conferma del Payment Intent: {}", e.getMessage(), e);
            return PaymentIntentResponseDto.error("Errore durante la conferma del pagamento: " + e.getUserMessage());
        } catch (Exception e) {
            log.error("Errore generico durante la conferma del Payment Intent: {}", e.getMessage(), e);
            return PaymentIntentResponseDto.error(INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Recupera lo stato di un Payment Intent
     */
    public PaymentIntentResponseDto getPaymentIntentStatus(String paymentIntentId) {
        try {
            log.info("Recupero stato Payment Intent: {}", paymentIntentId);
            
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            // Aggiorna il payment nel database
            updatePaymentFromStripe(paymentIntent);
            
            return PaymentIntentResponseDto.success(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    paymentIntent.getStatus()
            );
            
        } catch (StripeException e) {
            log.error("Errore Stripe durante il recupero del Payment Intent: {}", e.getMessage(), e);
            return PaymentIntentResponseDto.error("Errore durante il recupero del pagamento: " + e.getUserMessage());
        } catch (Exception e) {
            log.error("Errore generico durante il recupero del Payment Intent: {}", e.getMessage(), e);
            return PaymentIntentResponseDto.error(INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Crea o recupera un customer Stripe per l'utente
     */
    private String getOrCreateStripeCustomer(User user) throws StripeException {
        if (user.getStripeCustomerId() != null) {
            log.info("Utilizzo Stripe Customer esistente: {}", user.getStripeCustomerId());
            return user.getStripeCustomerId();
        }
        
        log.info("Creazione nuovo Stripe Customer per utente: {}", user.getEmail());
        
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getFirstName() + " " + user.getLastName())
                .setPhone(user.getPhone())
                .putMetadata("user_id", user.getId().toString())
                .build();
        
        Customer customer = Customer.create(params);
        
        // Salva l'ID del customer nell'utente
        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);
        
        log.info("Stripe Customer creato: {}", customer.getId());
        return customer.getId();
    }
    
    /**
     * Aggiorna un payment nel database con i dati da Stripe
     */
    private void updatePaymentFromStripe(PaymentIntent paymentIntent) {
        Optional<Payment> paymentOpt = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId());
        
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            
            // Mappa lo status di Stripe al nostro enum
            Payment.PaymentStatus status = mapStripeStatus(paymentIntent.getStatus());
            payment.setStatus(status);
            
            // Aggiorna altri campi se disponibili
            if (paymentIntent.getPaymentMethod() != null) {
                payment.setStripePaymentMethodId(paymentIntent.getPaymentMethod());
            }
            
            payment.setRawResponse(paymentIntent.toJson());
            
            paymentRepository.save(payment);
            
            // Se il pagamento è riuscito, aggiorna lo stato dell'ordine
            if (status == Payment.PaymentStatus.SUCCEEDED) {
                Order order = payment.getOrder();
                order.setStatus(Order.OrderStatus.PAID);
                orderRepository.save(order);
                log.info("Ordine {} marcato come pagato", order.getId());
            }
            
            log.info("Payment aggiornato: {} -> {}", payment.getId(), status);
        }
    }
    
    /**
     * Mappa gli stati di Stripe ai nostri enum
     */
    private Payment.PaymentStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "requires_payment_method" -> Payment.PaymentStatus.REQUIRES_PAYMENT_METHOD;
            case "requires_confirmation" -> Payment.PaymentStatus.REQUIRES_CONFIRMATION;
            case "requires_action" -> Payment.PaymentStatus.REQUIRES_ACTION;
            case "processing" -> Payment.PaymentStatus.PROCESSING;
            case "succeeded" -> Payment.PaymentStatus.SUCCEEDED;
            case "canceled" -> Payment.PaymentStatus.CANCELED;
            default -> Payment.PaymentStatus.FAILED;
        };
    }
}
