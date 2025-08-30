package com.samhap.kokomen.payment.external.dto;

import java.time.LocalDateTime;

public record TosspaymentsCancel(
        String transactionKey,
        String cancelReason,
        Long taxExemptionAmount,
        LocalDateTime canceledAt,
        Long easyPayDiscountAmount,
        String receiptKey,
        Long cancelAmount,
        Long taxFreeAmount,
        Long refundableAmount,
        String cancelStatus,
        String cancelRequestId
) {
}
