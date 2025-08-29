ALTER TABLE tosspayments_payment
    ADD COLUMN service_type ENUM('INTERVIEW') NOT NULL DEFAULT 'INTERVIEW' AFTER state;

ALTER TABLE tosspayments_payment
    ALTER COLUMN service_type DROP DEFAULT;