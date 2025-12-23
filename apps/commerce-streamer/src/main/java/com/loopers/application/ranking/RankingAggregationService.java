package com.loopers.application.ranking;

import com.loopers.ranking.RankingKey;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
@RequiredArgsConstructor
public class RankingAggregationService {
    private static final double LIKE_WEIGHT = 0.2d;
    private static final Duration TTL = Duration.ofDays(2);
    private static final double CARRY_OVER_WEIGHT = 0.1d;

    private final StringRedisTemplate redisTemplate;

    public void applyLike(long productId, Instant occurredAt) {
        LocalDate day = occurredAt.atZone(ZoneId.systemDefault()).toLocalDate();
        String key = RankingKey.dailyAll(day);
        String member = String.valueOf(productId);

        // Score = Weight * 1
        redisTemplate.opsForZSet().incrementScore(key, member, LIKE_WEIGHT);

        // test 설정
        redisTemplate.expire(key, TTL);
    }

    /**
     * 일별 랭킹에 대한 전일자 Carry Over 스케줄러
     * - 매일 23:30분 수행합니다.
     */
    @Scheduled(cron = "0 30 23 * * *")
    public void carryOverDailyRanking() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate tomorrow = today.plusDays(1);

        String sourceKey = RankingKey.dailyAll(today);
        String targetKey = RankingKey.dailyAll(tomorrow);

        carryOver(sourceKey, targetKey, CARRY_OVER_WEIGHT);
    }

    private void carryOver(String sourceKey, String targetKey, double weight) {
        redisTemplate.execute((RedisCallback<Long>) connection -> {
            byte[] target = redisTemplate.getStringSerializer().serialize(targetKey);
            byte[] source = redisTemplate.getStringSerializer().serialize(sourceKey);
            // ZUNIONSTORE dest 1 src WEIGHTS 0.1 AGGREGATE SUM
            Long result = (Long) connection.execute(
                    "ZUNIONSTORE",
                    target,
                    "1".getBytes(),
                    source,
                    "WEIGHTS".getBytes(),
                    String.valueOf(weight).getBytes(),
                    "AGGREGATE".getBytes(),
                    "SUM".getBytes()
            );
            // 순서 주의할 것
            // ZUNIONSTORE target ... → 키 재생성 되므로 이전 TTL 소멸됨
            connection.expire(target, TTL.getSeconds());
            return result;
        });
    }
}
