package com.loopers.batch.job.rankMonthly;

import com.loopers.batch.job.rankMonthly.dto.MonthlyAggRow;
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
public class RankMonthlyReaderConfig {
    private final DataSource dataSource;

    @Bean
    @StepScope
    public JdbcPagingItemReader<MonthlyAggRow> monthlyAggReader(
            @Value("#{jobParameters['startDate']}") String startDate,
            @Value("#{jobParameters['endDate']}") String endDate
    ) {
        return new JdbcPagingItemReaderBuilder<MonthlyAggRow>()
                .name("monthlyAggReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider())
                .parameterValues(Map.of(
                        "startDate", startDate,
                        "endDate", endDate
                ))
                .rowMapper((rs, rowNum) -> new MonthlyAggRow(
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
              product_id,
              view_count,
              like_count,
              order_count
            """);

        qp.setFromClause("""
            from (
                  select
                    pm.product_id as product_id,
                    SUM(CASE WHEN pm.metrics_type = 'VIEW' THEN pm.count ELSE 0 END) as view_count,
                    SUM(CASE WHEN pm.metrics_type = 'LIKE' THEN pm.count ELSE 0 END) as like_count,
                    SUM(CASE WHEN pm.metrics_type = 'ORDER_SUCCESS' THEN pm.count ELSE 0 END) as order_count
                  from product_metrics pm
                  where pm.metrics_date between :startDate and :endDate
                  group by pm.product_id
            ) t
            """);

        qp.setSortKeys(Map.of(
                "product_id", org.springframework.batch.item.database.Order.ASCENDING
        ));

        return qp;
    }


}
