package com.loopers.interfaces.api.pgVendor;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "pgClient",
        url = "http://localhost:8082"
)
public interface PgClient {
    @PostMapping("/api/v1/payments")
    void requestPayment(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PgPaymentV1Dto.Request request
    );

    @GetMapping("/api/v1/payments")
    void requestPaymentWithOrderId(
            @RequestHeader("X-USER-ID") String userId,
            @RequestParam("orderId") String orderId
    );
}
