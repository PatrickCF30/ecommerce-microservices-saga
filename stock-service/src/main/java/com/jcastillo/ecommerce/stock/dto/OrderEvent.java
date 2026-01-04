package com.jcastillo.ecommerce.stock.dto;

import java.io.Serializable;

public record OrderEvent(
        Long orderId,
        String productCode,
        Integer quantity,
        String type // "ORDER_CREATED", "ORDER_CANCELLED"
) implements Serializable {
}