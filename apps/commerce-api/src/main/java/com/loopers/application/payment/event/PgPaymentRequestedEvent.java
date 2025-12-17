package com.loopers.application.payment.event;

import com.loopers.application.payment.dto.PgPaymentCommand;

public record PgPaymentRequestedEvent(
        PgPaymentCommand command
) {
}
