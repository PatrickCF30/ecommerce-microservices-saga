package com.jcastillo.ecommerce.order.repository;

import com.jcastillo.ecommerce.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
