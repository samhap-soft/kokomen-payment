package com.samhap.kokomen.payment.controller;

import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.samhap.kokomen.global.BaseControllerTest;
import com.samhap.kokomen.payment.domain.PaymentState;
import com.samhap.kokomen.payment.domain.PaymentType;
import com.samhap.kokomen.payment.domain.ServiceType;
import com.samhap.kokomen.payment.domain.TosspaymentsPayment;
import com.samhap.kokomen.payment.domain.TosspaymentsPaymentResult;
import com.samhap.kokomen.payment.domain.TosspaymentsStatus;
import com.samhap.kokomen.payment.repository.TosspaymentsPaymentRepository;
import com.samhap.kokomen.payment.repository.TosspaymentsPaymentResultRepository;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

class PaymentControllerTest extends BaseControllerTest {

    @Autowired
    private TosspaymentsPaymentResultRepository tosspaymentsPaymentResultRepository;

    @Autowired
    private TosspaymentsPaymentRepository tosspaymentsPaymentRepository;

    // TODO: 메타데이터 인터뷰쪽 코드 보고 수정 필요
    @Test
    void 나의_결제_내역을_조회한다() throws Exception {
        // given
        Long memberId = 1L;
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("MEMBER_ID", memberId);

        // 완료된 결제
        TosspaymentsPayment completedPayment = createPayment(memberId, "ORDER_20241201_001", "인터뷰 결제 1", 5000L, PaymentState.COMPLETED);
        createPaymentResult(completedPayment, TosspaymentsStatus.DONE, "카드", LocalDateTime.now().minusDays(1), null, null);

        // 취소된 결제
        TosspaymentsPayment canceledPayment = createPayment(memberId, "ORDER_20241201_002", "인터뷰 결제 2", 10000L, PaymentState.CANCELED);
        createPaymentResult(canceledPayment, TosspaymentsStatus.CANCELED, "카드", null, null, null);

        // 실패한 결제
        TosspaymentsPayment failedPayment = createPayment(memberId, "ORDER_20241201_003", "인터뷰 결제 3", 7000L, PaymentState.CLIENT_BAD_REQUEST);
        createPaymentResult(failedPayment, TosspaymentsStatus.ABORTED, null, null, "INVALID_CARD", "유효하지 않은 카드입니다.");

        String expectedResponseJson = """
                {
                  "content": [
                    {
                      "payment_key": "test_payment_key_ORDER_20241201_003",
                      "order_id": "ORDER_20241201_003",
                      "order_name": "인터뷰 결제 3",
                      "total_amount": 7000,
                      "state": "CLIENT_BAD_REQUEST",
                      "service_type": "INTERVIEW",
                      "metadata": "{\\"test\\": \\"metadata\\"}",
                      "failure_code": "INVALID_CARD",
                      "failure_message": "유효하지 않은 카드입니다."
                    },
                    {
                      "payment_key": "test_payment_key_ORDER_20241201_002",
                      "order_id": "ORDER_20241201_002",
                      "order_name": "인터뷰 결제 2",
                      "total_amount": 10000,
                      "state": "CANCELED",
                      "service_type": "INTERVIEW",
                      "method": "카드",
                      "metadata": "{\\"test\\": \\"metadata\\"}"
                    },
                    {
                      "payment_key": "test_payment_key_ORDER_20241201_001",
                      "order_id": "ORDER_20241201_001",
                      "order_name": "인터뷰 결제 1",
                      "total_amount": 5000,
                      "state": "COMPLETED",
                      "service_type": "INTERVIEW",
                      "method": "카드",
                      "metadata": "{\\"test\\": \\"metadata\\"}"
                    }
                  ],
                  "pageable": {
                    "page_number": 0,
                    "page_size": 10,
                    "sort": {
                      "empty": false,
                      "sorted": true,
                      "unsorted": false
                    },
                    "offset": 0,
                    "paged": true,
                    "unpaged": false
                  },
                  "last": true,
                  "total_pages": 1,
                  "total_elements": 3,
                  "size": 10,
                  "number": 0,
                  "sort": {
                    "empty": false,
                    "sorted": true,
                    "unsorted": false
                  },
                  "first": true,
                  "number_of_elements": 3,
                  "empty": false
                }
                """;

        // when & then
        mockMvc.perform(get("/api/v1/payments/me")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc")
                        .header("Cookie", "JSESSIONID=" + session.getId())
                        .session(session)
                        .cookie(new Cookie("JSESSIONID", session.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponseJson, false))
                .andDo(document("payment-findMyPayments",
                        requestCookies(
                                cookieWithName("JSESSIONID").description("로그인 세션을 위한 JSESSIONID 쿠키")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (기본값: 0)"),
                                parameterWithName("size").description("페이지 크기 (기본값: 10)"),
                                parameterWithName("sort").description("정렬 기준 (기본값: id,desc)")
                        ),
                        responseFields(
                                fieldWithPath("content").description("결제 내역 목록"),
                                fieldWithPath("content[].payment_key").description("토스페이먼츠 결제 키"),
                                fieldWithPath("content[].order_id").description("주문 ID"),
                                fieldWithPath("content[].order_name").description("주문명"),
                                fieldWithPath("content[].total_amount").description("결제 금액"),
                                fieldWithPath("content[].state").description(
                                        "결제 상태 " + Arrays.asList(PaymentState.COMPLETED, PaymentState.CANCELED, PaymentState.CLIENT_BAD_REQUEST)),
                                fieldWithPath("content[].service_type").description("서비스 타입 " + Arrays.asList(ServiceType.values())),
                                fieldWithPath("content[].created_at").description("결제 생성 일시"),
                                fieldWithPath("content[].approved_at").description("결제 승인 일시 (완료된 경우에만)").optional(),
                                fieldWithPath("content[].method").description("결제 수단 (완료된 경우에만)").optional(),
                                fieldWithPath("content[].metadata").description("결제 메타데이터"),
                                fieldWithPath("content[].failure_code").description("실패 코드 (실패한 경우에만)").optional(),
                                fieldWithPath("content[].failure_message").description("실패 메시지 (실패한 경우에만)").optional(),
                                fieldWithPath("pageable").description("페이지 정보"),
                                fieldWithPath("pageable.page_number").description("현재 페이지 번호"),
                                fieldWithPath("pageable.page_size").description("페이지 크기"),
                                fieldWithPath("pageable.sort").description("정렬 정보"),
                                fieldWithPath("pageable.sort.empty").description("정렬 정보가 비어있는지 여부"),
                                fieldWithPath("pageable.sort.sorted").description("정렬되어 있는지 여부"),
                                fieldWithPath("pageable.sort.unsorted").description("정렬되어 있지 않은지 여부"),
                                fieldWithPath("pageable.offset").description("오프셋"),
                                fieldWithPath("pageable.paged").description("페이지네이션 적용 여부"),
                                fieldWithPath("pageable.unpaged").description("페이지네이션 미적용 여부"),
                                fieldWithPath("last").description("마지막 페이지인지 여부"),
                                fieldWithPath("total_pages").description("전체 페이지 수"),
                                fieldWithPath("total_elements").description("전체 요소 수"),
                                fieldWithPath("size").description("페이지 크기"),
                                fieldWithPath("number").description("현재 페이지 번호"),
                                fieldWithPath("sort").description("정렬 정보"),
                                fieldWithPath("sort.empty").description("정렬 정보가 비어있는지 여부"),
                                fieldWithPath("sort.sorted").description("정렬되어 있는지 여부"),
                                fieldWithPath("sort.unsorted").description("정렬되어 있지 않은지 여부"),
                                fieldWithPath("first").description("첫 번째 페이지인지 여부"),
                                fieldWithPath("number_of_elements").description("현재 페이지의 요소 수"),
                                fieldWithPath("empty").description("결과가 비어있는지 여부")
                        )
                ));
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
