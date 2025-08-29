package com.samhap.kokomen.payment.repository;

import com.samhap.kokomen.payment.domain.PaymentState;
import com.samhap.kokomen.payment.domain.TosspaymentsPaymentResult;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TosspaymentsPaymentResultRepository extends JpaRepository<TosspaymentsPaymentResult, Long> {

    Page<TosspaymentsPaymentResult> findByTosspaymentsPaymentMemberIdAndTosspaymentsPaymentStateIn(
            Long memberId,
            List<PaymentState> states,
            Pageable pageable
    );
}
