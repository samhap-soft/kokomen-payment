package com.samhap.kokomen.payment.service;

import com.samhap.kokomen.global.exception.BadRequestException;
import com.samhap.kokomen.payment.domain.PaymentState;
import com.samhap.kokomen.payment.domain.TosspaymentsPayment;
import com.samhap.kokomen.payment.domain.TosspaymentsPaymentResult;
import com.samhap.kokomen.payment.external.TosspaymentsClient;
import com.samhap.kokomen.payment.external.TosspaymentsInternalServerErrorCode;
import com.samhap.kokomen.payment.external.dto.Failure;
import com.samhap.kokomen.payment.external.dto.TosspaymentsPaymentCancelRequest;
import com.samhap.kokomen.payment.external.dto.TosspaymentsPaymentResponse;
import com.samhap.kokomen.payment.service.dto.CancelRequest;
import com.samhap.kokomen.payment.service.dto.ConfirmRequest;
import com.samhap.kokomen.payment.service.dto.PaymentResponse;
import java.net.SocketTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentFacadeService {

    private final TosspaymentsTransactionService tosspaymentsTransactionService;
    private final TosspaymentsPaymentService tosspaymentsPaymentService;
    private final TosspaymentsClient tosspaymentsClient;

    public PaymentResponse confirmPayment(ConfirmRequest request) {
        TosspaymentsPayment tosspaymentsPayment = tosspaymentsPaymentService.saveTosspaymentsPayment(request);
        try {
            TosspaymentsPaymentResponse tosspaymentsPaymentResponse = confirmPayment(request, tosspaymentsPayment);
            return PaymentResponse.from(tosspaymentsPaymentResponse);
        } catch (Exception e) {
            tosspaymentsPaymentService.updateState(tosspaymentsPayment.getId(), PaymentState.NEED_CANCEL);
            throw e;
        }
    }

    private TosspaymentsPaymentResponse confirmPayment(ConfirmRequest request, TosspaymentsPayment tosspaymentsPayment) {
        try {
            TosspaymentsPaymentResponse tosspaymentsConfirmResponse = tosspaymentsClient.confirmPayment(request.toTosspaymentsConfirmRequest());
            tosspaymentsPayment.validateTosspaymentsResult(tosspaymentsConfirmResponse.paymentKey(), tosspaymentsConfirmResponse.orderId(),
                    tosspaymentsConfirmResponse.totalAmount(), tosspaymentsConfirmResponse.metadata());
            TosspaymentsPaymentResult tosspaymentsPaymentResult = tosspaymentsConfirmResponse.toTosspaymentsPaymentResult(tosspaymentsPayment);
            tosspaymentsTransactionService.applyTosspaymentsPaymentResult(tosspaymentsPaymentResult, PaymentState.COMPLETED);
            return tosspaymentsConfirmResponse;
        } catch (HttpClientErrorException e) {
            // 토스에서 400 에러를 응답하면 TosspaymentsPaymentResponse 스펙이 아니라 {"code": ..., "message": ...} 스펙으로만 응답이 온다.
            Failure failure = e.getResponseBodyAs(Failure.class);
            String code = failure.code();
            log.info("토스 결제 실패(400), code = " + code + ", message = " + failure.message());
            // TODO: 기존 로직 제대로 돌아가도록 반영
//            TosspaymentsPaymentResponse tosspaymentsConfirmResponse = e.getResponseBodyAs(TosspaymentsPaymentResponse.class);
//            String code = tosspaymentsConfirmResponse.failure().code();
//            if (TosspaymentsInternalServerErrorCode.contains(code)) {
//                TosspaymentsPaymentResult tosspaymentsPaymentResult = tosspaymentsConfirmResponse.toTosspaymentsPaymentResult(tosspaymentsPayment);
//                tosspaymentsTransactionService.applyTosspaymentsPaymentResult(tosspaymentsPaymentResult, PaymentState.SERVER_BAD_REQUEST);
//                throw e;
//            }
//
//            tosspaymentsPaymentService.updateState(tosspaymentsPayment.getId(), PaymentState.CLIENT_BAD_REQUEST);
//            throw new BadRequestException(tosspaymentsConfirmResponse.failure().message(), e);
            throw new BadRequestException(failure.message(), e);
        } catch (HttpServerErrorException e) {
            // TODO: retry
            TosspaymentsPaymentResponse tosspaymentsConfirmResponse = e.getResponseBodyAs(TosspaymentsPaymentResponse.class);
            TosspaymentsPaymentResult tosspaymentsPaymentResult = tosspaymentsConfirmResponse.toTosspaymentsPaymentResult(tosspaymentsPayment);
            tosspaymentsTransactionService.applyTosspaymentsPaymentResult(tosspaymentsPaymentResult, PaymentState.NEED_CANCEL);
            throw e;
        } catch (ResourceAccessException e) {
            if (e.getRootCause() instanceof SocketTimeoutException) {
                SocketTimeoutException socketTimeoutException = (SocketTimeoutException) e.getRootCause();
                if (socketTimeoutException.getMessage().contains("Connect timed out")) {
                    // TODO: retry
                    tosspaymentsPaymentService.updateState(tosspaymentsPayment.getId(), PaymentState.CONNECTION_TIMEOUT);
                    throw e;
                }
                if (socketTimeoutException.getMessage().contains("Read timed out")) {
                    // TODO: retry
                    tosspaymentsPaymentService.updateState(tosspaymentsPayment.getId(), PaymentState.NEED_CANCEL);
                    throw e;
                }
            }

            throw e;
        }
    }

    public void cancelPayment(CancelRequest request) {
        TosspaymentsPaymentCancelRequest tosspaymentsPaymentCancelRequest = new TosspaymentsPaymentCancelRequest(request.cancelReason());
        TosspaymentsPaymentResponse response = tosspaymentsClient.cancelPayment(request.paymentKey(), tosspaymentsPaymentCancelRequest);
        tosspaymentsTransactionService.applyCancelResult(response);
    }
}
