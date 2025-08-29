ALTER TABLE tosspayments_payment_result
    ADD COLUMN failure_message TEXT AFTER failure_code;