package com.samhap.kokomen.payment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.samhap.kokomen.global.BaseTest;
import com.samhap.kokomen.payment.domain.PaymentState;
import com.samhap.kokomen.payment.domain.PaymentType;
import com.samhap.kokomen.payment.domain.ServiceType;
import com.samhap.kokomen.payment.domain.TosspaymentsPayment;
import com.samhap.kokomen.payment.domain.TosspaymentsPaymentResult;
import com.samhap.kokomen.payment.domain.TosspaymentsStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class TosspaymentsPaymentResultRepositoryTest extends BaseTest {

    @Autowired
    private TosspaymentsPaymentResultRepository tosspaymentsPaymentResultRepository;

    @Autowired
    private TosspaymentsPaymentRepository tosspaymentsPaymentRepository;

    @Test
    void 회원_ID와_결제_상태로_결제_결과를_조회할_수_있다() {
        // given
        Long memberId = 1L;
        
        // 완료된 결제
        TosspaymentsPayment completedPayment = createPayment(memberId, "ORDER_001", "완료된 결제", 5000L, PaymentState.COMPLETED);
        createPaymentResult(completedPayment, TosspaymentsStatus.DONE, "카드", LocalDateTime.now(), null, null);
        
        // 취소된 결제
        TosspaymentsPayment canceledPayment = createPayment(memberId, "ORDER_002", "취소된 결제", 10000L, PaymentState.CANCELED);
        createPaymentResult(canceledPayment, TosspaymentsStatus.CANCELED, "카드", null, null, null);
        
        // 실패한 결제
        TosspaymentsPayment failedPayment = createPayment(memberId, "ORDER_003", "실패한 결제", 7000L, PaymentState.CLIENT_BAD_REQUEST);
        createPaymentResult(failedPayment, TosspaymentsStatus.ABORTED, null, null, "INVALID_CARD", "유효하지 않은 카드");
        
        // 다른 회원의 결제 (조회되면 안 됨)
        TosspaymentsPayment otherMemberPayment = createPayment(2L, "ORDER_004", "다른 회원 결제", 3000L, PaymentState.COMPLETED);
        createPaymentResult(otherMemberPayment, TosspaymentsStatus.DONE, "카드", LocalDateTime.now(), null, null);
        
        List<PaymentState> allowedStates = List.of(
                PaymentState.COMPLETED,
                PaymentState.CANCELED,
                PaymentState.CLIENT_BAD_REQUEST
        );
        
        // when
        Pageable pageable = PageRequest.of(0, 10);
        var results = tosspaymentsPaymentResultRepository
                .findByTosspaymentsPaymentMemberIdAndTosspaymentsPaymentStateIn(memberId, allowedStates, pageable);

        // then
        assertThat(results.getContent()).hasSize(3);
        assertThat(results.getTotalElements()).isEqualTo(3);
        assertThat(results.getTotalPages()).isEqualTo(1);
    }

    @Test
    void 실패_메시지가_포함된_결제_결과를_조회할_수_있다() {
        // given
        Long memberId = 1L;
        
        // 실패한 결제 1
        TosspaymentsPayment failedPayment1 = createPayment(memberId, "ORDER_FAIL_001", "카드 오류 결제", 5000L, PaymentState.CLIENT_BAD_REQUEST);
        createPaymentResult(failedPayment1, TosspaymentsStatus.ABORTED, null, null, "INVALID_CARD", "유효하지 않은 카드입니다.");
        
        // 실패한 결제 2
        TosspaymentsPayment failedPayment2 = createPayment(memberId, "ORDER_FAIL_002", "잔액 부족 결제", 10000L, PaymentState.CLIENT_BAD_REQUEST);
        createPaymentResult(failedPayment2, TosspaymentsStatus.ABORTED, null, null, "INSUFFICIENT_FUNDS", "잔액이 부족합니다.");
        
        // 완료된 결제 (조회되면 안 됨)
        TosspaymentsPayment completedPayment = createPayment(memberId, "ORDER_SUCCESS_001", "성공한 결제", 7000L, PaymentState.COMPLETED);
        createPaymentResult(completedPayment, TosspaymentsStatus.DONE, "카드", LocalDateTime.now(), null, null);
        
        List<PaymentState> allowedStates = List.of(PaymentState.CLIENT_BAD_REQUEST);
        
        // when
        Pageable pageable = PageRequest.of(0, 10);
        var results = tosspaymentsPaymentResultRepository
                .findByTosspaymentsPaymentMemberIdAndTosspaymentsPaymentStateIn(memberId, allowedStates, pageable);

        // then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getTotalElements()).isEqualTo(2);
        
        var content = results.getContent();
        assertThat(content).extracting(TosspaymentsPaymentResult::getFailureCode)
                .containsExactlyInAnyOrder("INVALID_CARD", "INSUFFICIENT_FUNDS");
        assertThat(content).extracting(TosspaymentsPaymentResult::getFailureMessage)
                .containsExactlyInAnyOrder("유효하지 않은 카드입니다.", "잔액이 부족합니다.");
    }

    private TosspaymentsPayment createPayment(Long memberId, String orderId, String orderName, Long totalAmount, PaymentState state) {
        TosspaymentsPayment payment = new TosspaymentsPayment(
                "test_payment_key_" + orderId,
                memberId,
                orderId,
                orderName,
                totalAmount,
                "{\"test\": \"metadata\"}",
                ServiceType.INTERVIEW
        );
        payment.updateState(state);
        return tosspaymentsPaymentRepository.save(payment);
    }

    private TosspaymentsPaymentResult createPaymentResult(TosspaymentsPayment payment, TosspaymentsStatus status,
                                                          String method, LocalDateTime approvedAt,
                                                          String failureCode, String failureMessage) {
        TosspaymentsPaymentResult result = new TosspaymentsPaymentResult(
                payment,
                PaymentType.NORMAL,
                "tvivarepublica",
                "KRW",
                payment.getTotalAmount(),
                method,
                status == TosspaymentsStatus.DONE ? payment.getTotalAmount() : 0L,
                status,
                LocalDateTime.now().minusMinutes(5),
                approvedAt,
                status == TosspaymentsStatus.DONE ? "test_transaction_key" : null,
                status == TosspaymentsStatus.DONE ? 9091L : 0L,
                status == TosspaymentsStatus.DONE ? 909L : 0L,
                0L,
                0L,
                false,
                status == TosspaymentsStatus.DONE ? "https://receipt.toss.im/receipt" : null,
                null,
                null,
                null,
                "KR",
                failureCode,
                failureMessage
        );
        return tosspaymentsPaymentResultRepository.save(result);
    }
}