package com.jcastillo.ecommerce.stock.util;

import com.jcastillo.ecommerce.stock.entity.Stock;
import com.jcastillo.ecommerce.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockDataLoader implements CommandLineRunner {

    private final StockRepository stockRepository;

    @Override
    public void run(String... args) {
        if (stockRepository.count() == 0) {
            stockRepository.save(Stock.builder().code("PROD-001").quantity(100).build());
            stockRepository.save(Stock.builder().code("PROD-002").quantity(5).build());
            log.info("📦 Stock inicial cargado: PROD-001 (100u) y PROD-002 (5u)");
        }
    }
}