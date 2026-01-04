package com.jcastillo.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "stock-service")
public interface StockClient {

    // Este método debe ser IDÉNTICO al del StockController
    @PostMapping("/api/stock/deduct")
    boolean deductStock(@RequestParam String code, @RequestParam Integer quantity);
}