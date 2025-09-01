package com.samhap.kokomen.payment.service;

import com.samhap.kokomen.payment.domain.PaymentState;
import com.samhap.kokomen.payment.domain.TosspaymentsPayment;
import com.samhap.kokomen.payment.repository.TosspaymentsPaymentRepository;
import com.samhap.kokomen.payment.service.dto.ConfirmRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TosspaymentsPaymentService {

    private final TosspaymentsPaymentRepository tosspaymentsPaymentRepository;

    @Transactional
    public TosspaymentsPayment saveTosspaymentsPayment(ConfirmRequest request) {
        return tosspaymentsPaymentRepository.save(request.toTosspaymentsPayment());
    }

    @Transactional(readOnly = true)
    public TosspaymentsPayment readById(Long id) {
        return tosspaymentsPaymentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("해당 id의 결제 정보가 존재하지 않습니다. id: " + id));
    }

    @Transactional(readOnly = true)
    public TosspaymentsPayment readByPaymentKey(String paymentKey) {
        return tosspaymentsPaymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalStateException("해당 paymentKey의 결제 정보가 존재하지 않습니다. paymentKey: " + paymentKey));
    }

    @Transactional
    public void updateState(Long tosspaymentsPaymentId, PaymentState state) {
        TosspaymentsPayment tosspaymentsPayment = readById(tosspaymentsPaymentId);
        tosspaymentsPayment.updateState(state);
    }
}
