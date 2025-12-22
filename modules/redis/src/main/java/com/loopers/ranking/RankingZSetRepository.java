package com.loopers.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redis ZSET 쓰기(zincrby + ttl + pipeline)
 */
@Repository
@RequiredArgsConstructor
public class RankingZSetRepository {
    private final StringRedisTemplate redisTemplate;

    public List<RankingEntry> findTopDailyAllByLimit(LocalDate date, int limit) {
        String key = RankingKey.dailyAll(date);

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);

        List<RankingEntry> result = new ArrayList<>();

        // TODO 랭킹정보가 없다면?
        if(tuples == null) {
            return result;
        }

        for(ZSetOperations.TypedTuple<String> tuple : tuples) {
            if(tuple.getValue() == null) continue;
            long productId = Long.parseLong(tuple.getValue());
            double score = tuple.getScore() == null ? 0d : tuple.getScore();
            result.add(new RankingEntry(productId, score));
        }
        return result;
    }
}
