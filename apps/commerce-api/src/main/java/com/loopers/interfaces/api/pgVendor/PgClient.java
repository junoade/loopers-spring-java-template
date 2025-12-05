package com.loopers.interfaces.api.pgVendor;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "pgClient",
        url = "http://localhost:8082"
)
public interface PgClient {
    @PostMapping("/api/v1/payments")
    void requestPayment(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PgPaymentV1Dto.Request request);
}
