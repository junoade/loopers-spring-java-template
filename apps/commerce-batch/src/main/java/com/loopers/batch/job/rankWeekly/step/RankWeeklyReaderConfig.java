package com.loopers.batch.job.rankWeekly.step;

import com.loopers.batch.job.rankWeekly.step.dto.WeeklyAggRow;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class RankWeeklyReaderConfig {
    private final DataSource dataSource;

    @Bean
    @StepScope
    public JdbcPagingItemReader<WeeklyAggRow> weeklyAggReader(
            @Value("#{jobParameters['yearWeek']}") String yearWeek) {
        return new JdbcPagingItemReaderBuilder<WeeklyAggRow>()
                .name("weeklyAggReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider())
                .parameterValues(Map.of("yearWeek", yearWeek))
                .rowMapper((rs, rowNum) -> new WeeklyAggRow(
                        rs.getString("year_week"),
                        rs.getLong("product_id"),
                        rs.getLong("view_count"),
                        rs.getLong("like_count"),
                        rs.getLong("order_count")
                ))
                .pageSize(1000)
                .build();
    }

    private MySqlPagingQueryProvider queryProvider() {
        MySqlPagingQueryProvider qp = new MySqlPagingQueryProvider();

        qp.setSelectClause("""
            select
              year_week,
              product_id,
              view_count,
              like_count,
              order_count
            """);

        qp.setFromClause("""
            from (
              select
                CONCAT(
                  SUBSTRING(YEARWEEK(STR_TO_DATE(pm.metrics_date, '%Y%m%d'), 3), 1, 4),
                  '-W',
                  LPAD(SUBSTRING(YEARWEEK(STR_TO_DATE(pm.metrics_date, '%Y%m%d'), 3), 5, 2), 2, '0')
                ) as year_week,
                pm.product_id as product_id,
                SUM(CASE WHEN pm.metrics_type = 'VIEW' THEN pm.count ELSE 0 END) as view_count,
                SUM(CASE WHEN pm.metrics_type = 'LIKE' THEN pm.count ELSE 0 END) as like_count,
                SUM(CASE WHEN pm.metrics_type = 'ORDER_SUCCESS' THEN pm.count ELSE 0 END) as order_count
              from product_metrics pm
              group by year_week, pm.product_id
            ) t
            """);

        qp.setWhereClause("where t.year_week = :yearWeek");

        qp.setSortKeys(Map.of(
                "product_id", org.springframework.batch.item.database.Order.ASCENDING
        ));

        return qp;
    }


}
