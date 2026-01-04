package com.jcastillo.ecommerce.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nombres clave para la mensajería
    public static final String EXCHANGE = "saga_exchange";
    public static final String STOCK_QUEUE = "stock_queue";
    public static final String ROUTING_KEY_STOCK = "routing_stock";

    public static final String ORDER_RESULT_QUEUE = "order_result_queue";
    public static final String ROUTING_KEY_RESULT = "routing_order_result";

    // 1. Crear la Cola de Stock
    @Bean
    public Queue stockQueue() {
        return new Queue(STOCK_QUEUE);
    }

    // 2. Crear el Exchange
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // 3. Unir la Cola al Exchange
    @Bean
    public Binding bindingStock(Queue stockQueue, TopicExchange exchange) {
        return BindingBuilder.bind(stockQueue).to(exchange).with(ROUTING_KEY_STOCK);
    }

    // 4. Configurar convertidor a JSON
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    // Definir la cola de resultados
    @Bean
    public Queue resultQueue() {
        return new Queue(ORDER_RESULT_QUEUE);
    }

    // Unir la cola de resultados al Exchange
    @Bean
    public Binding bindingResult(Queue resultQueue, TopicExchange exchange) {
        return BindingBuilder.bind(resultQueue).to(exchange).with(ROUTING_KEY_RESULT);
    }
}