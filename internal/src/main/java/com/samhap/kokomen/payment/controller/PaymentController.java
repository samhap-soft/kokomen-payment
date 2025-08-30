package com.samhap.kokomen.payment.controller;

import com.samhap.kokomen.payment.service.PaymentFacadeService;
import com.samhap.kokomen.payment.service.dto.CancelRequest;
import com.samhap.kokomen.payment.service.dto.ConfirmRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/internal/v1/payments")
@RestController
public class PaymentController {

    private final PaymentFacadeService paymentFacadeService;

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmPayment(@RequestBody @Valid ConfirmRequest request) {
        paymentFacadeService.confirmPayment(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelPayment(@RequestBody @Valid CancelRequest request) {
        paymentFacadeService.cancelPayment(request);
        return ResponseEntity.noContent().build();
    }
}
