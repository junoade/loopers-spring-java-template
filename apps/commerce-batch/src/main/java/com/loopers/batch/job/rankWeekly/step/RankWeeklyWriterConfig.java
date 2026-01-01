package com.loopers.batch.job.rankWeekly.step;

import com.loopers.batch.job.rankWeekly.step.dto.WeeklyRankRow;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class RankWeeklyWriterConfig {
    private final DataSource dataSource;

    @Bean
    public JdbcBatchItemWriter<WeeklyRankRow> weeklyMvUpsertWriter() {
        String sql = """
            INSERT INTO mv_product_rank_weekly
              (year_week_key, product_id, view_count, like_count, order_count, score, created_at, updated_at)
            VALUES
              (:yearWeek, :productId, :viewCount, :likeCount, :orderCount, :score, NOW(6), NOW(6))
            ON DUPLICATE KEY UPDATE
              view_count = VALUES(view_count),
              like_count = VALUES(like_count),
              order_count = VALUES(order_count),
              score=VALUES(score),
              updated_at = NOW(6)
            """;

        return new JdbcBatchItemWriterBuilder<WeeklyRankRow>()
                .dataSource(dataSource)
                .sql(sql)
                .beanMapped()
                .assertUpdates(false)
                .build();
    }
}
