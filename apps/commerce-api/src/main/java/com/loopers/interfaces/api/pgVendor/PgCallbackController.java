package com.loopers.interfaces.api.pgVendor;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PgCallbackController {
    private final OrderService orderService;

    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(@RequestBody PgPaymentV1Dto.Response callback) {
        log.info("callback = {}", callback);

        if(callback.status() == null || callback.status().isBlank()) {
            return ResponseEntity.badRequest().body(assembleResponse("INVALID"));
        }

        String status = callback.status();
        long orderId = Long.parseLong(callback.orderId());

        if(!orderService.isPending(orderId)) {
            log.error("Order id {} is not pending", orderId);
            return ResponseEntity.ok(assembleResponse("FAILED"));
        }

        if ("SUCCESS".equals(status)) {
            orderService.updateOrderStatus(orderId, OrderStatus.SUCCESS);
            log.info("OrderId : {}, Order status updated to SUCCESS", orderId);
        } else if("FAILED".equals(status)) {
            orderService.updateOrderStatus(orderId, OrderStatus.FAILED);
            log.error("OrderId : {}, Order status updated to Failed Because {}", orderId, callback.reason());
        } else {
            log.error("OrderId : {}, Callback replied unexpected status: {}", orderId, status);
        }

        return ResponseEntity.ok(assembleResponse("SUCCESS"));
    }

    private Map<String, Object> assembleResponse(String response) {
        return Map.of(
                "meta", Map.of(
                        "result", response
                )
        );
    }

    /*@PostMapping("/callback")
    public ResponseEntity<String> callbackRaw(HttpServletRequest request) throws Exception {
        String raw = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        log.error("RAW CALLBACK BODY = {}", raw);
        return ResponseEntity.ok("OK");
    }*/
}
