package com.loopers.domain.tracking.general;

/**
 * 내부 시스템에서 발생한 이벤트에 대한 액터 타입을 정의합니다.
 * User : 사용자
 * System : 내부 시스템
 * PG_Vender : PG사 외부 시스템
 */
public enum EventActorType {
    USER,
    SYSTEM,
    PG_VENDOR
}
