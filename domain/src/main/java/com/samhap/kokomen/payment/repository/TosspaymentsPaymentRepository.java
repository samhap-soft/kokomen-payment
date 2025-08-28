package com.samhap.kokomen.payment.repository;

import com.samhap.kokomen.payment.domain.TosspaymentsPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TosspaymentsPaymentRepository extends JpaRepository<TosspaymentsPayment, Long> {
}
