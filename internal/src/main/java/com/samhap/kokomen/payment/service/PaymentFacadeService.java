package com.samhap.kokomen.payment.service;

import com.samhap.kokomen.global.exception.BadRequestException;
import com.samhap.kokomen.payment.domain.PaymentState;
import com.samhap.kokomen.payment.domain.TosspaymentsPayment;
import com.samhap.kokomen.payment.domain.TosspaymentsPaymentResult;
import com.samhap.kokomen.payment.external.TosspaymentsClient;
import com.samhap.kokomen.payment.external.TosspaymentsInternalServerErrorCode;
import com.samhap.kokomen.payment.external.dto.TosspaymentsConfirmResponse;
import com.samhap.kokomen.payment.service.dto.ConfirmRequest;
import java.net.SocketTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@RequiredArgsConstructor
@Service
public class PaymentFacadeService {

    private final TosspaymentsTransactionService tosspaymentsTransactionService;
    private final TosspaymentsPaymentService tosspaymentsPaymentService;
    private final TosspaymentsClient tosspaymentsClient;

    public void confirmPayment(ConfirmRequest request) {
        TosspaymentsPayment tosspaymentsPayment = tosspaymentsPaymentService.saveTosspaymentsPayment(request);
        try {
            confirmPayment(request, tosspaymentsPayment);
        } catch (Exception e) {
            tosspaymentsPaymentService.updateState(tosspaymentsPayment.getId(), PaymentState.NEED_CANCEL);
            throw e;
        }
    }

    private TosspaymentsConfirmResponse confirmPayment(ConfirmRequest request, TosspaymentsPayment tosspaymentsPayment) {
        try {
            TosspaymentsConfirmResponse tosspaymentsConfirmResponse = tosspaymentsClient.confirmPayment(request.toTosspaymentsConfirmRequest());
            tosspaymentsPayment.validateTosspaymentsResult(tosspaymentsConfirmResponse.paymentKey(), tosspaymentsConfirmResponse.orderId(),
                    tosspaymentsConfirmResponse.totalAmount(), tosspaymentsConfirmResponse.metadata());
            TosspaymentsPaymentResult tosspaymentsPaymentResult = tosspaymentsConfirmResponse.toTosspaymentsPaymentResult(tosspaymentsPayment);
            tosspaymentsTransactionService.applyTosspaymentsPaymentResult(tosspaymentsPaymentResult, PaymentState.APPROVED);
            return tosspaymentsConfirmResponse;
        } catch (HttpClientErrorException e) {
            TosspaymentsConfirmResponse tosspaymentsConfirmResponse = e.getResponseBodyAs(TosspaymentsConfirmResponse.class);
            String code = tosspaymentsConfirmResponse.failure().code();
            if (TosspaymentsInternalServerErrorCode.contains(code)) {
                TosspaymentsPaymentResult tosspaymentsPaymentResult = tosspaymentsConfirmResponse.toTosspaymentsPaymentResult(tosspaymentsPayment);
                tosspaymentsTransactionService.applyTosspaymentsPaymentResult(tosspaymentsPaymentResult, PaymentState.SERVER_BAD_REQUEST);
                throw e;
            }

            tosspaymentsPaymentService.updateState(tosspaymentsPayment.getId(), PaymentState.CLIENT_BAD_REQUEST);
            throw new BadRequestException(tosspaymentsConfirmResponse.failure().message(), e);
        } catch (HttpServerErrorException e) {
            // TODO: retry
            TosspaymentsConfirmResponse tosspaymentsConfirmResponse = e.getResponseBodyAs(TosspaymentsConfirmResponse.class);
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

    // 토스에서 실패 응답, 우리 상태가 실패(토페가 500 응답, 토페 타임아웃 -> 재시도 후 또 안되면 바로 Failed)
}
