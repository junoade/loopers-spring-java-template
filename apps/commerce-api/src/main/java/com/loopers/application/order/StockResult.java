package com.loopers.application.order;

import java.util.List;

public record StockResult(
            List<OrderCommand.OrderLine> successLines,
            List<OrderCommand.OrderLine> failedLines,
            long requiringPrice,
            long errorPrice
    ) {
        public static StockResult of(
                List<OrderCommand.OrderLine> successLines,
                List<OrderCommand.OrderLine> failedLines,
                long requiringPrice,
                long errorPrice
        )  {
            return new StockResult(successLines,
                    failedLines,
                    requiringPrice,
                    errorPrice);
        }

        public StockResult withRequiringPrice(long newRequiringPrice) {
            return new StockResult(
                    this.successLines,
                    this.failedLines,
                    newRequiringPrice,
                    this.errorPrice
            );
        }


}
