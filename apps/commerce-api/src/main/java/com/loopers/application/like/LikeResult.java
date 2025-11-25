package com.loopers.application.like;

public record LikeResult (
        String userId,
        Long productId
) {
    public static LikeResult of(String userId, Long productId) {
        return new LikeResult(userId, productId);
    }
}
