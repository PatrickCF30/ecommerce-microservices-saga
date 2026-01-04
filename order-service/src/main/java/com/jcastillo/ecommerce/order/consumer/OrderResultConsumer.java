package com.jcastillo.ecommerce.order.consumer;

import com.jcastillo.ecommerce.order.config.RabbitMQConfig;
import com.jcastillo.ecommerce.order.dto.OrderEvent;
import com.jcastillo.ecommerce.order.entity.Order;
import com.jcastillo.ecommerce.order.repository.OrderRepository;
import com.jcastillo.ecommerce.order.service.PaymentOrchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderResultConsumer {

    private final OrderRepository orderRepository;
    private final PaymentOrchestrator paymentOrchestrator; // Inyectamos el orquestador

    @RabbitListener(queues = RabbitMQConfig.ORDER_RESULT_QUEUE)
    public void consumeResult(OrderEvent event) {
        log.info("📩 Mensaje recibido de Stock: {}", event);

        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) return;

        if ("STOCK_REJECTED".equals(event.type())) {
            order.setStatus("REJECTED_NO_STOCK");
            orderRepository.save(order);
            log.info("❌ Orden #{} rechazada por inventario.", order.getId());
        }
        else if ("STOCK_CONFIRMED".equals(event.type())) {
            log.info("✅ Stock confirmado para orden #{}. Delegando pago al orquestador...", order.getId());

            order.setStatus("STOCK_RESERVED");
            orderRepository.save(order);

            paymentOrchestrator.pagar(order);
        }
    }
}