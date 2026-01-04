package com.jcastillo.ecommerce.payment.repository;

import com.jcastillo.ecommerce.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}