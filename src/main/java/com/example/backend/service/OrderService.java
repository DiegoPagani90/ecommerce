package com.example.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.Cart;
import com.example.backend.model.CartItem;
import com.example.backend.model.Order;
import com.example.backend.model.OrderItem;
import com.example.backend.model.Product;
import com.example.backend.model.User;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    
    /**
     * Crea un nuovo ordine dal carrello dell'utente
     */
    public Order createOrderFromCart(Long userId, Long shippingAddressId, String notes) {
        log.info("Creazione ordine dal carrello per utente: {}", userId);
        
        // Verifica utente
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato: " + userId));
        
        // Ottieni carrello attivo
        Optional<Cart> cartOpt = cartService.getActiveCart(userId);
        if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty()) {
            throw new RuntimeException("Carrello vuoto, impossibile creare ordine");
        }
        
        Cart cart = cartOpt.get();
        
        // Verifica disponibilità prodotti e calcola totale
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            // Verifica stock disponibile
            if (product.getStockQty() < cartItem.getQuantity()) {
                throw new RuntimeException("Stock insufficiente per il prodotto: " + product.getName());
            }
            
            // Crea OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }
        
        // Crea ordine
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order.setSubtotalAmount(totalAmount); // Per ora stesso valore del totale
        
        // Se è fornito l'ID dell'indirizzo di spedizione, associalo
        if (shippingAddressId != null) {
            // Qui dovremmo avere un AddressRepository per recuperare l'indirizzo
            // Per ora saltiamo questa parte
        }
        
        order.setNotes(notes);
        
        // Salva ordine
        Order savedOrder = orderRepository.save(order);
        
        // Associa OrderItems all'ordine
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(savedOrder);
        }
        savedOrder.setItems(orderItems);
        
        // Aggiorna stock prodotti
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            product.setStockQty(product.getStockQty() - cartItem.getQuantity());
            productRepository.save(product);
            
            log.info("Stock aggiornato per prodotto {}: {} -> {}", 
                    product.getName(), 
                    product.getStockQty() + cartItem.getQuantity(), 
                    product.getStockQty());
        }
        
        // Marca carrello come checked out
        cartService.markCartAsCheckedOut(userId);
        
        log.info("Ordine {} creato con successo per utente {}", savedOrder.getId(), userId);
        return orderRepository.save(savedOrder);
    }
    
    /**
     * Ottiene un ordine per ID
     */
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long orderId) {
        log.info("Ricerca ordine con ID: {}", orderId);
        return orderRepository.findById(orderId);
    }
    
    /**
     * Ottiene un ordine con dettagli completi
     */
    @Transactional(readOnly = true)
    public Optional<Order> getOrderWithDetails(Long orderId) {
        log.info("Ricerca ordine con dettagli per ID: {}", orderId);
        return orderRepository.findByIdWithItems(orderId);
    }
    
    /**
     * Ottiene tutti gli ordini di un utente
     */
    @Transactional(readOnly = true)
    public List<Order> getUserOrders(Long userId) {
        log.info("Ricerca ordini per utente: {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Ottiene gli ordini di un utente con paginazione
     */
    @Transactional(readOnly = true)
    public Page<Order> getUserOrdersPaginated(Long userId, Pageable pageable) {
        log.info("Ricerca ordini paginati per utente: {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * Ottiene tutti gli ordini con un determinato stato
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        log.info("Ricerca ordini con stato: {}", status);
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    /**
     * Aggiorna lo stato di un ordine
     */
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus, String statusNote) {
        log.info("Aggiornamento stato ordine {} a: {}", orderId, newStatus);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato: " + orderId));
        
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        if (statusNote != null && !statusNote.trim().isEmpty()) {
            String currentNotes = order.getNotes() != null ? order.getNotes() : "";
            order.setNotes(currentNotes + "\n[" + java.time.LocalDateTime.now() + "] " + statusNote);
        }
        
        // Se ordine viene cancellato, ripristina stock
        if (newStatus == Order.OrderStatus.CANCELLED && oldStatus != Order.OrderStatus.CANCELLED) {
            restoreStockForOrder(order);
        }
        
        Order savedOrder = orderRepository.save(order);
        log.info("Stato ordine {} aggiornato da {} a {}", orderId, oldStatus, newStatus);
        
        return savedOrder;
    }
    
    /**
     * Cancella un ordine (solo se in stato PENDING)
     */
    public Order cancelOrder(Long orderId, String reason) {
        log.info("Cancellazione ordine: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Impossibile cancellare ordine in stato: " + order.getStatus());
        }
        
        return updateOrderStatus(orderId, Order.OrderStatus.CANCELLED, "Ordine cancellato: " + reason);
    }
    
    /**
     * Conferma un ordine (da PENDING a CONFIRMED)
     */
    public Order confirmOrder(Long orderId) {
        log.info("Conferma ordine: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Impossibile confermare ordine in stato: " + order.getStatus());
        }
        
        return updateOrderStatus(orderId, Order.OrderStatus.CONFIRMED, "Ordine confermato");
    }
    
    /**
     * Marca un ordine come spedito
     */
    public Order shipOrder(Long orderId, String trackingNumber) {
        log.info("Spedizione ordine: {} con tracking: {}", orderId, trackingNumber);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException("Impossibile spedire ordine in stato: " + order.getStatus());
        }
        
        order.setTrackingNumber(trackingNumber);
        return updateOrderStatus(orderId, Order.OrderStatus.SHIPPED, "Ordine spedito - Tracking: " + trackingNumber);
    }
    
    /**
     * Marca un ordine come consegnato
     */
    public Order deliverOrder(Long orderId) {
        log.info("Consegna ordine: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.SHIPPED) {
            throw new RuntimeException("Impossibile consegnare ordine in stato: " + order.getStatus());
        }
        
        return updateOrderStatus(orderId, Order.OrderStatus.DELIVERED, "Ordine consegnato");
    }
    
    /**
     * Ripristina lo stock per un ordine cancellato
     */
    private void restoreStockForOrder(Order order) {
        log.info("Ripristino stock per ordine cancellato: {}", order.getId());
        
        if (order.getItems() != null) {
            for (OrderItem orderItem : order.getItems()) {
                Product product = orderItem.getProduct();
                product.setStockQty(product.getStockQty() + orderItem.getQuantity());
                productRepository.save(product);
                
                log.info("Stock ripristinato per prodotto {}: +{} = {}", 
                        product.getName(), 
                        orderItem.getQuantity(), 
                        product.getStockQty());
            }
        }
    }
    
    /**
     * Calcola statistiche ordini per utente
     */
    @Transactional(readOnly = true)
    public OrderStatistics getUserOrderStatistics(Long userId) {
        log.info("Calcolo statistiche ordini per utente: {}", userId);
        
        Long totalOrders = orderRepository.countByUserId(userId);
        BigDecimal totalSpent = orderRepository.sumTotalAmountByUserId(userId);
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;
        
        return new OrderStatistics(totalOrders, totalSpent);
    }
    
    /**
     * Classe per statistiche ordini
     */
    public static class OrderStatistics {
        private final Long totalOrders;
        private final BigDecimal totalSpent;
        
        public OrderStatistics(Long totalOrders, BigDecimal totalSpent) {
            this.totalOrders = totalOrders;
            this.totalSpent = totalSpent;
        }
        
        public Long getTotalOrders() { return totalOrders; }
        public BigDecimal getTotalSpent() { return totalSpent; }
    }
}
