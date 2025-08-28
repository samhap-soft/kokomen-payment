package com.samhap.kokomen.payment.service;

import com.samhap.kokomen.payment.domain.TosspaymentsPaymentResult;
import com.samhap.kokomen.payment.repository.TosspaymentsPaymentResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TosspaymentsPaymentResultService {

    private final TosspaymentsPaymentResultRepository tosspaymentsPaymentResultRepository;

    @Transactional
    public TosspaymentsPaymentResult save(TosspaymentsPaymentResult tosspaymentsPaymentResult) {
        return tosspaymentsPaymentResultRepository.save(tosspaymentsPaymentResult);
    }
}
