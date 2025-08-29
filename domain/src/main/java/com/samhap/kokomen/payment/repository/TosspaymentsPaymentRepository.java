package com.samhap.kokomen.payment.repository;

import com.samhap.kokomen.payment.domain.TosspaymentsPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TosspaymentsPaymentRepository extends JpaRepository<TosspaymentsPayment, Long> {

    @Query("""
        SELECT p FROM TosspaymentsPayment p
        LEFT JOIN FETCH p.tosspaymentsPaymentResult
        WHERE p.memberId = :memberId
        ORDER BY p.createdAt DESC
        """)
    Page<TosspaymentsPayment> findByMemberIdWithResult(@Param("memberId") Long memberId, Pageable pageable);
}
