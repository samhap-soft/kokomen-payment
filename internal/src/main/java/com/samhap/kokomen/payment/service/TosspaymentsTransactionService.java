package com.samhap.kokomen.payment.service;

import com.samhap.kokomen.payment.domain.PaymentState;
import com.samhap.kokomen.payment.domain.TosspaymentsPayment;
import com.samhap.kokomen.payment.domain.TosspaymentsPaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TosspaymentsTransactionService {

    private final TosspaymentsPaymentService tosspaymentsPaymentService;
    private final TosspaymentsPaymentResultService tosspaymentsPaymentResultService;

    @Transactional
    public TosspaymentsPaymentResult applyTosspaymentsPaymentResult(TosspaymentsPaymentResult tosspaymentsPaymentResult, PaymentState paymentState) {
        TosspaymentsPayment tosspaymentsPayment = tosspaymentsPaymentService.readById(tosspaymentsPaymentResult.getTosspaymentsPayment().getId());
        tosspaymentsPayment.updateState(paymentState);
        return tosspaymentsPaymentResultService.save(tosspaymentsPaymentResult);
    }
}
