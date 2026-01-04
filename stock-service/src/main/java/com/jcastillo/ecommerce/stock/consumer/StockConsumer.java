package com.jcastillo.ecommerce.stock.consumer;

import com.jcastillo.ecommerce.stock.dto.OrderEvent;
import com.jcastillo.ecommerce.stock.entity.Stock;
import com.jcastillo.ecommerce.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockConsumer {

    private final StockRepository stockRepository;
    private final RabbitTemplate rabbitTemplate;

    // Escucha la cola "stock_queue" definida en el otro servicio
    @RabbitListener(queues = "stock_queue")
    public void consumeOrderEvent(OrderEvent event) {
        log.info("📩 Evento recibido en Stock Service: {}", event);

        if ("ORDER_CREATED".equals(event.type())) {
            procesarReserva(event);
        } else if ("ORDER_CANCELLED".equals(event.type())) {
            procesarDevolucion(event);
        }
    }

    private void procesarReserva(OrderEvent event) {
        Stock stock = stockRepository.findByCode(event.productCode()).orElse(null);

        // CASO FALLIDO
        if (stock == null || stock.getQuantity() < event.quantity()) {
            log.warn("❌ Stock insuficiente para producto: {}", event.productCode());
            OrderEvent rejectEvent = new OrderEvent(event.orderId(), event.productCode(), event.quantity(), "STOCK_REJECTED");
            rabbitTemplate.convertAndSend("saga_exchange", "routing_order_result", rejectEvent);
            return;
        }

        // CASO EXITOSO
        stock.setQuantity(stock.getQuantity() - event.quantity());
        stockRepository.save(stock);
        log.info("✅ Stock reservado. Quedan: {}", stock.getQuantity());

        // 1. Crear evento de confirmación
        OrderEvent confirmEvent = new OrderEvent(
                event.orderId(),
                event.productCode(),
                event.quantity(),
                "STOCK_CONFIRMED"
        );

        // 2. Enviar confirmación a Order Service
        rabbitTemplate.convertAndSend(
                "saga_exchange",
                "routing_order_result",
                confirmEvent
        );
        log.info("📤 Confirmación enviada a Order Service para orden #{}", event.orderId());
    }

    private void procesarDevolucion(OrderEvent event) {
        log.info("↩️ Devolviendo stock...");
        Stock stock = stockRepository.findByCode(event.productCode()).orElse(null);
        if (stock != null) {
            stock.setQuantity(stock.getQuantity() + event.quantity());
            stockRepository.save(stock);
            log.info("✅ Stock restaurado. Actual: {}", stock.getQuantity());
        }
    }
}