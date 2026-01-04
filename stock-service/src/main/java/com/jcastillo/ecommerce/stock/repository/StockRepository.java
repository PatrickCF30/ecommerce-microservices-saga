package com.jcastillo.ecommerce.stock.repository;

import com.jcastillo.ecommerce.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByCode(String code);
}