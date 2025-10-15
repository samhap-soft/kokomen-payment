package com.samhap.kokomen.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Profile("local")
@RequiredArgsConstructor
@Controller
public class PaymentTestController {

    @GetMapping("/payment-test")
    public String paymentTestPage() {
        return "payment-test";
    }

    @GetMapping("/payment/success")
    public String paymentSuccessPage() {
        return "payment-success";
    }

    @GetMapping("/payment/fail")
    public String paymentFailPage() {
        return "payment-fail";
    }

    @GetMapping("/payment/refund")
    public String paymentRefundPage() {
        return "payment-refund";
    }
}
