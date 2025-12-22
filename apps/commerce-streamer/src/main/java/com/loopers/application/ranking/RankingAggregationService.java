package com.loopers.application.ranking;

import com.loopers.ranking.RankingKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class RankingAggregationService {
    private static final double LIKE_WEIGHT = 0.2d;
    private static final Duration TTL = Duration.ofDays(2);

    private final StringRedisTemplate redisTemplate;

    public void applyLike(long productId, Instant occurredAt) {
        LocalDate day = occurredAt.atZone(ZoneOffset.UTC).toLocalDate();
        String key = RankingKey.dailyAll(day);
        String member = String.valueOf(productId);

        // Score = Weight * 1
        redisTemplate.opsForZSet().incrementScore(key, member, LIKE_WEIGHT);

        // test 설정
        redisTemplate.expire(key, TTL);
    }
}
