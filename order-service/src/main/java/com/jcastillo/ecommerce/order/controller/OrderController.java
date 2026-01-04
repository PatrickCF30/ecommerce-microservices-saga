package com.jcastillo.ecommerce.order.controller;

import com.jcastillo.ecommerce.order.config.RabbitMQConfig;
import com.jcastillo.ecommerce.order.dto.OrderEvent; // Importa tu nuevo DTO
import com.jcastillo.ecommerce.order.entity.Order;
import com.jcastillo.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping
    public String placeOrder(@RequestBody Order order) {
        // 1. Guardar en BD (Estado PENDIENTE)
        order.setStatus("PENDING");
        orderRepository.save(order);

        // 2. Crear el Evento
        // Pasamos los datos al constructor en Orden
        OrderEvent event = new OrderEvent(
                order.getId(),           // orderId
                order.getProductCode(),  // productCode
                order.getQuantity(),     // quantity
                "ORDER_CREATED"          // type
        );

        log.info("📤 Enviando evento a RabbitMQ: {}", event);

        // 3. ENVIAR mensaje a la cola (Async)
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_STOCK,
                event
        );

        return "⏳ Orden recibida y enviada a proceso. ID: " + order.getId();
    }
}