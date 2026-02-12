package com.samhap.kokomen.payment.service;

import com.samhap.kokomen.global.exception.BadRequestException;
import com.samhap.kokomen.global.exception.InternalServerErrorException;
import com.samhap.kokomen.global.exception.KokomenException;
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
        } catch (KokomenException e) {
            // inner에서 상태 처리 완료 (BadRequestException, InternalServerErrorException)
            throw e;
        } catch (HttpServerErrorException | ResourceAccessException e) {
            // inner에서 상태 처리 완료 (NEED_CANCEL, CONNECTION_TIMEOUT)
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외만 NEED_CANCEL 설정
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
            Failure failure = e.getResponseBodyAs(Failure.class);
            String code = failure.code();

            if (TosspaymentsInternalServerErrorCode.contains(code)) {
                log.error("토스 결제 실패(서버 원인 400), code = {}, message = {}", code, failure.message());
                tosspaymentsPaymentService.updateState(tosspaymentsPayment.getId(), PaymentState.SERVER_BAD_REQUEST);
                throw new InternalServerErrorException("결제 처리 중 서버 오류가 발생했습니다.", e);
            }

            log.info("토스 결제 실패(클라이언트 원인 400), code = {}, message = {}", code, failure.message());
            tosspaymentsPaymentService.updateState(tosspaymentsPayment.getId(), PaymentState.CLIENT_BAD_REQUEST);
            throw new BadRequestException(failure.message(), e);
        } catch (HttpServerErrorException e) {
            // TODO: retry
            try {
                TosspaymentsPaymentResponse tosspaymentsConfirmResponse = e.getResponseBodyAs(TosspaymentsPaymentResponse.class);
                TosspaymentsPaymentResult tosspaymentsPaymentResult = tosspaymentsConfirmResponse.toTosspaymentsPaymentResult(tosspaymentsPayment);
                tosspaymentsTransactionService.applyTosspaymentsPaymentResult(tosspaymentsPaymentResult, PaymentState.NEED_CANCEL);
            } catch (Exception parseException) {
                log.warn("토스 5xx 응답 파싱 실패, 상태만 업데이트합니다. paymentId = {}", tosspaymentsPayment.getId(), parseException);
                tosspaymentsPaymentService.updateState(tosspaymentsPayment.getId(), PaymentState.NEED_CANCEL);
            }
            throw e;
        } catch (ResourceAccessException e) {
            if (e.getRootCause() instanceof SocketTimeoutException socketTimeoutException) {
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
        try {
            TosspaymentsPaymentResponse response = tosspaymentsClient.cancelPayment(request.paymentKey(), tosspaymentsPaymentCancelRequest);
            tosspaymentsTransactionService.applyCancelResult(response);
        } catch (HttpClientErrorException e) {
            Failure failure = e.getResponseBodyAs(Failure.class);
            log.error("결제 취소 실패(400) - paymentKey: {}, code: {}, message: {}", request.paymentKey(), failure.code(), failure.message());
            throw new BadRequestException(failure.message(), e);
        } catch (HttpServerErrorException e) {
            log.error("결제 취소 실패(5xx) - paymentKey: {}, status: {}", request.paymentKey(), e.getStatusCode());
            throw new InternalServerErrorException("결제 취소 처리 중 서버 오류가 발생했습니다.", e);
        } catch (ResourceAccessException e) {
            log.error("결제 취소 네트워크 오류 - paymentKey: {}", request.paymentKey(), e);
            throw new InternalServerErrorException("결제 취소 처리 중 네트워크 오류가 발생했습니다.", e);
        }
    }
}
