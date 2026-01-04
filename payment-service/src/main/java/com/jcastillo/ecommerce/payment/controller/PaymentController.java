package com.jcastillo.ecommerce.payment.controller;

import com.jcastillo.ecommerce.payment.entity.Payment;
import com.jcastillo.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @PostMapping("/process")
    public boolean processPayment(@RequestParam Long orderId, @RequestParam BigDecimal amount) {
        log.info("💳 Payment Service: Procesando pago de $ {} para Orden ID: {}", amount, orderId);

        // LÓGICA DE SIMULACIÓN:
        // Si el monto es mayor a $5000, rechazamos el pago (simula falta de fondos)
        if (amount.compareTo(new BigDecimal("5000")) > 0) {
            log.info("❌ Pago rechazado: Fondos insuficientes.");
            return false;
        }

        // Si es menor, aprobamos
        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .status("PAID")
                .timestamp(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
        log.info("✅ Pago exitoso.");
        return true;
    }
}