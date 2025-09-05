package com.samhap.kokomen.payment.domain;

import com.samhap.kokomen.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tosspayments_payment", indexes = {
        @Index(name = "idx_payment_member_id", columnList = "member_id")
})
public class TosspaymentsPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_key", nullable = false, unique = true)
    private String paymentKey;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "order_name", nullable = false)
    private String orderName;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "metadata", columnDefinition = "json", nullable = false)
    private String metadata;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentState state;

    @Column(name = "service_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    public TosspaymentsPayment(String paymentKey, Long memberId, String orderId, String orderName, Long totalAmount, String metadata, ServiceType serviceType) {
        this.paymentKey = paymentKey;
        this.memberId = memberId;
        this.orderId = orderId;
        this.orderName = orderName;
        this.totalAmount = totalAmount;
        this.metadata = metadata;
        this.serviceType = serviceType;
        this.state = PaymentState.NEED_APPROVE;
    }

    public void updateState(PaymentState state) {
        this.state = state;
    }

    public void validateTosspaymentsResult(String paymentKey, String orderId, Long totalAmount, String metadata) {
        if (!this.paymentKey.equals(paymentKey)) {
            throw new IllegalStateException("토스 페이먼츠 응답(%s)의 paymentKey가 DB에 저장된 값(%s)과 다릅니다.".formatted(paymentKey, this.paymentKey));
        }
        if (!this.orderId.equals(orderId)) {
            throw new IllegalStateException("토스 페이먼츠 응답(%s)의 orderId가 DB에 저장된 값(%s)과 다릅니다.".formatted(orderId, this.orderId));
        }
        if (!this.totalAmount.equals(totalAmount)) {
            throw new IllegalStateException("토스 페이먼츠 응답(%d)의 totalAmount가 DB에 저장된 값(%d)과 다릅니다.".formatted(totalAmount, this.totalAmount));
        }
//        if (!this.metadata.equals(metadata)) {
//            throw new IllegalStateException("토스 페이먼츠 응답(%s)의 metadata가 DB에 저장된 값(%s)과 다릅니다.".formatted(metadata, this.metadata));
//        }
    }
}
