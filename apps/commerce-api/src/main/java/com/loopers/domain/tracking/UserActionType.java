package com.loopers.domain.tracking;

/**
 * 공통 유저 행동 이벤트 모델을 정의합니다.
 */
public enum UserActionType {
    PRODUCT_VIEW,        // 상품 상세 조회
    PRODUCT_LIST_VIEW,   // 상품 목록 조회
    PRODUCT_LIKE,
    PRODUCT_UNLIKE,
    ORDER_CREATED,
    ORDER_PAID,
    ORDER_FAILED,
    PAYMENT_REQUESTED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED
}
