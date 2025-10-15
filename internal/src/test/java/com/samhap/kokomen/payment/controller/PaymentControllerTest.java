package com.samhap.kokomen.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.samhap.kokomen.global.BaseControllerTest;
import com.samhap.kokomen.payment.domain.PaymentType;
import com.samhap.kokomen.payment.domain.TosspaymentsStatus;
import com.samhap.kokomen.payment.service.PaymentFacadeService;
import com.samhap.kokomen.payment.service.dto.CancelRequest;
import com.samhap.kokomen.payment.service.dto.ConfirmRequest;
import com.samhap.kokomen.payment.service.dto.PaymentResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PaymentControllerTest extends BaseControllerTest {

    @MockitoBean
    private PaymentFacadeService paymentFacadeService;

    @Test
    void 결제를_승인한다() throws Exception {
        // given
        PaymentResponse mockResponse = new PaymentResponse(
                "test_payment_key_001",
                PaymentType.NORMAL,
                "ORDER_20241201_001",
                "꼬꼬면 토큰 10개",
                "tvivarepublica",
                "KRW",
                "카드",
                10000L,
                10000L,
                TosspaymentsStatus.DONE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "test_transaction_key",
                9091L,
                909L,
                0L,
                0L,
                true,
                "{\"productType\":\"KOKOMEN_TOKEN\",\"quantity\":\"10\"}",
                null,
                null,
                null,
                "KR",
                null,
                null
        );

        when(paymentFacadeService.confirmPayment(any(ConfirmRequest.class))).thenReturn(mockResponse);

        String requestJson = """
                {
                    "payment_key": "test_payment_key_001",
                    "order_id": "ORDER_20241201_001",
                    "total_amount": 10000,
                    "order_name": "꼬꼬면 토큰 10개",
                    "member_id": 1,
                    "metadata": {
                        "productType": "KOKOMEN_TOKEN",
                        "quantity": "10"
                    },
                    "service_type": "INTERVIEW"
                }
                """;

        // when & then
        mockMvc.perform(post("/internal/v1/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    void 결제를_취소한다() throws Exception {
        // given
        doNothing().when(paymentFacadeService).cancelPayment(any(CancelRequest.class));

        String requestJson = """
                {
                    "payment_key": "test_payment_key_001",
                    "cancel_reason": "단순 변심"
                }
                """;

        // when & then
        mockMvc.perform(post("/internal/v1/payments/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());
    }
}
