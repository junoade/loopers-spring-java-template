package com.loopers.domain.tracking;

import java.time.Instant;
import java.util.Map;

/**
 * 유저의 액션 이벤트를 정의합니다.
 * @param eventId UUID
 * @param actor 대상
 * @param action 행동
 * @param entityType 대상 엔티티
 * @param entityId 대상 엔티티의 PK
 * @param channel
 * @param correlationId 상위키
 * @param traceId 요청 단위 trace
 * @param occurredAt 발생시간
 * @param attributes 주요 파라미터 정보(price, category, filter등)
 */
public record UserActionEvent(
        String eventId,
        EventActorType actor,
        UserActionType action,
        String entityType,
        String entityId,
        String channel,
        String correlationId,
        String traceId,
        Instant occurredAt,
        Map<String, Object> attributes
) {
}
