package com.samhap.kokomen.payment.service;

import com.samhap.kokomen.global.dto.MemberAuth;
import com.samhap.kokomen.payment.domain.PaymentState;
import com.samhap.kokomen.payment.dto.MyPaymentResponse;
import com.samhap.kokomen.payment.repository.TosspaymentsPaymentResultRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private static final List<PaymentState> FINISHED_PAYMENT_STATES = List.of(
            PaymentState.CANCELED,
            PaymentState.CLIENT_BAD_REQUEST,
            PaymentState.COMPLETED
    );
    private final TosspaymentsPaymentResultRepository tosspaymentsPaymentResultRepository;

    @Transactional(readOnly = true)
    public Page<MyPaymentResponse> findMyPayments(MemberAuth memberAuth, Pageable pageable) {
        return tosspaymentsPaymentResultRepository
                .findByTosspaymentsPaymentMemberIdAndTosspaymentsPaymentStateIn(memberAuth.memberId(), FINISHED_PAYMENT_STATES, pageable)
                .map(MyPaymentResponse::from);
    }
}
