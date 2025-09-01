package com.samhap.kokomen.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.samhap.kokomen.global.BaseControllerTest;
import com.samhap.kokomen.payment.service.PaymentFacadeService;
import com.samhap.kokomen.payment.service.dto.CancelRequest;
import com.samhap.kokomen.payment.service.dto.ConfirmRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PaymentControllerTest extends BaseControllerTest {

    @MockitoBean
    private PaymentFacadeService paymentFacadeService;

    @Test
    void 결제를_승인한다() throws Exception {
        // given
        doNothing().when(paymentFacadeService).confirmPayment(any(ConfirmRequest.class));

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
                .andExpect(status().isNoContent());
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
