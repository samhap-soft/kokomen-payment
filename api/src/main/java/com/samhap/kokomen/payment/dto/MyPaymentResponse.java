package com.samhap.kokomen.payment.dto;

import com.samhap.kokomen.payment.domain.PaymentState;
import com.samhap.kokomen.payment.domain.ServiceType;
import com.samhap.kokomen.payment.domain.TosspaymentsPayment;
import com.samhap.kokomen.payment.domain.TosspaymentsPaymentResult;
import java.time.LocalDateTime;

public record MyPaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        Long totalAmount,
        PaymentState state,
        ServiceType serviceType,
        LocalDateTime createdAt,
        LocalDateTime approvedAt,
        String method,
        String metadata,
        String failureCode,
        String failureMessage
) {

    public static MyPaymentResponse from(TosspaymentsPaymentResult result) {
        TosspaymentsPayment payment = result.getTosspaymentsPayment();

        return new MyPaymentResponse(
                payment.getPaymentKey(),
                payment.getOrderId(),
                payment.getOrderName(),
                payment.getTotalAmount(),
                payment.getState(),
                payment.getServiceType(),
                payment.getCreatedAt(),
                result.getApprovedAt(),
                result.getMethod(),
                payment.getMetadata(),
                result.getFailureCode(),
                result.getFailureMessage()
        );
    }
}
