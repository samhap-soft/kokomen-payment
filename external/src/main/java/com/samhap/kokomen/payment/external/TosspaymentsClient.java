package com.samhap.kokomen.payment.external;

import com.samhap.kokomen.global.annotation.ExecutionTimer;
import com.samhap.kokomen.payment.external.dto.TosspaymentsConfirmRequest;
import com.samhap.kokomen.payment.external.dto.TosspaymentsPaymentCancelRequest;
import com.samhap.kokomen.payment.external.dto.TosspaymentsPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@ExecutionTimer
@Component
public class TosspaymentsClient {

    private final RestClient restClient;

    public TosspaymentsClient(TossPaymentsClientBuilder tossPaymentsClientBuilder) {
        this.restClient = tossPaymentsClientBuilder.getTossPaymentsClientBuilder().build();
    }

    public TosspaymentsPaymentResponse confirmPayment(TosspaymentsConfirmRequest request) {
        try {
            // TODO: interceptor로 로깅 처리
            log.info("토스페이먼츠 결제 승인 API 호출 - paymentKey: {}, orderId: {}, amount: {}",
                    request.paymentKey(), request.orderId(), request.amount());

            TosspaymentsPaymentResponse response = restClient.post()
                    .uri("/v1/payments/confirm")
                    .body(request)
                    .retrieve()
                    .body(TosspaymentsPaymentResponse.class);
            log.info("토스페이먼츠 결제 승인 완료 - response: {}", response);

            return response;

        } catch (HttpClientErrorException e) {
            log.error("토스페이먼츠 결제 승인 실패 - paymentKey: {}, status: {}, responseBody: {}",
                    request.paymentKey(), e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }

    public TosspaymentsPaymentResponse cancelPayment(String paymentKey, TosspaymentsPaymentCancelRequest request) {
        try {
            log.info("토스페이먼츠 결제 취소 API 호출 - paymentKey: {}, cancelReason: {}",
                    paymentKey, request.cancelReason());

            TosspaymentsPaymentResponse response = restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .body(request)
                    .retrieve()
                    .body(TosspaymentsPaymentResponse.class);
            log.info("토스페이먼츠 결제 취소 완료 - response: {}", response);

            return response;

        } catch (HttpClientErrorException e) {
            log.error("토스페이먼츠 결제 취소 실패 - paymentKey: {}, status: {}, responseBody: {}",
                    paymentKey, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }
}
