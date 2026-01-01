package com.loopers.batch.job.rankMonthly;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RankMonthlyProcessorConfig {
    @Bean
    @StepScope
    public RankMonthlyProcessor rankMonthlyProcessor(
            @Value("#{jobParameters['yearMonth']}") String yearMonth
    ) {
        return new RankMonthlyProcessor(yearMonth);
    }
}
