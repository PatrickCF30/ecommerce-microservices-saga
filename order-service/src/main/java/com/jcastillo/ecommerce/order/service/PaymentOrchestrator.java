package com.jcastillo.ecommerce.order.service;

import com.jcastillo.ecommerce.order.client.PaymentClient;
import com.jcastillo.ecommerce.order.config.RabbitMQConfig;
import com.jcastillo.ecommerce.order.dto.OrderEvent;
import com.jcastillo.ecommerce.order.entity.Order;
import com.jcastillo.ecommerce.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentOrchestrator {

    private final PaymentClient paymentClient;
    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @CircuitBreaker(name = "paymentBreaker", fallbackMethod = "fallbackPago")
    public void pagar(Order order) {
        log.info("💳 Intentando conectar con Pasarela de Pagos...");

        boolean paymentSuccess = paymentClient.processPayment(order.getId(), order.getPrice());

        if (paymentSuccess) {
            order.setStatus("APPROVED");
            orderRepository.save(order);
            log.info("🏆 Orden #{} FINALIZADA con éxito.", order.getId());
        } else {
            iniciarCompensacion(order, "PAYMENT_REJECTED");
        }
    }

    // El método fallback debe ser público para que Resilience4j lo encuentre
    public void fallbackPago(Order order, Throwable t) {
        log.error("🔥 CIRCUIT BREAKER ACTIVADO: Payment Service no responde. Error: {}", t.getMessage());
        iniciarCompensacion(order, "PAYMENT_SERVICE_DOWN");
    }

    private void iniciarCompensacion(Order order, String motivo) {
        log.warn("⚠️ Pago fallido ({}). Iniciando devolución de stock...", motivo);
        order.setStatus("CANCELLED_" + motivo);
        orderRepository.save(order);

        OrderEvent rollbackEvent = new OrderEvent(
                order.getId(), order.getProductCode(), order.getQuantity(), "ORDER_CANCELLED"
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_STOCK, rollbackEvent);
        log.info("↩️ Solicitud de compensación enviada...");
    }
}