package com.loopers.application.ranking;

import com.loopers.ranking.RankingKey;
import com.loopers.testcontainers.RedisTestContainersConfig;
import com.loopers.utils.RedisCleanUp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Import(RedisTestContainersConfig.class)
@SpringBootTest
class RankingAggregationServiceTest {

    @Autowired
    RedisCleanUp redisCleanUp;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RankingAggregationService rankingAggregationService;

    @BeforeEach
    void setUp() {
        redisCleanUp.truncateAll();
    }

    @Test
    @DisplayName("일별 랭킹 carry over 테스트 - 오늘 ZSET 점수를 0.1 가중치로 내일 키로 복사하고 TTL을 설정한다")
    void carryOver_should_copy_weighted_scores_to_tomorrow_key() {
        // given
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate tomorrow = today.plusDays(1);

        String sourceKey = RankingKey.dailyAll(today);
        String targetKey = RankingKey.dailyAll(tomorrow);

        // source ZSET 준비 (productId=11: 6.0, productId=22: 1.5)
        redisTemplate.opsForZSet().add(sourceKey, "11", 6.0);
        redisTemplate.opsForZSet().add(sourceKey, "22", 1.5);

        // when
        rankingAggregationService.carryOverDailyRanking();

        // then (0.1 가중치 적용)
        Double s11 = redisTemplate.opsForZSet().score(targetKey, "11");
        Double s22 = redisTemplate.opsForZSet().score(targetKey, "22");

        assertThat(s11).isNotNull();
        assertThat(s22).isNotNull();

        assertThat(s11).isCloseTo(0.6, Assertions.offset(1e-9));
        assertThat(s22).isCloseTo(0.15, Assertions.offset(1e-9));

        // TTL 확인 (expire를 먼저 걸고 unionstore를 하므로 TTL이 존재해야 함)
        Long ttl = redisTemplate.getExpire(targetKey); // seconds
        assertThat(ttl).isNotNull();
        assertThat(ttl).isGreaterThan(0);
    }
}
