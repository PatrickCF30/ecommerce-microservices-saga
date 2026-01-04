package com.jcastillo.ecommerce.order.dto; // (Ajusta el paquete según corresponda)

import java.io.Serializable;

public record OrderEvent(
        Long orderId,
        String productCode,
        Integer quantity,
        String type // "ORDER_CREATED", "ORDER_CANCELLED"
) implements Serializable {
}