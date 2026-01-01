package com.loopers.batch.job.rankMonthly;

import com.loopers.batch.job.rankMonthly.dto.MonthlyAggRow;
import com.loopers.batch.job.rankMonthly.dto.MonthlyRankRow;
import com.loopers.batch.job.rankWeekly.step.dto.WeeklyAggRow;
import com.loopers.batch.listener.JobListener;
import com.loopers.batch.listener.StepMonitorListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = "rankMonthlyMvJob")
@Configuration
// @EnableBatchProcessing
@RequiredArgsConstructor
public class RankMonthlyJobConfig {
    private final JobListener jobListener;
    private final StepMonitorListener stepMonitorListener;

    @Bean
    public Job rankMonthlyMvJob(JobRepository jobRepository, @Qualifier("rankMonthlyMvStep") Step rankWeeklyMvStep) {
        return new JobBuilder("rankMonthlyMvJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(rankWeeklyMvStep)
                .listener(jobListener)
                .build();
    }

    @Bean
    public Step rankMonthlyMvStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcPagingItemReader<MonthlyAggRow> weeklyAggReader,
            RankMonthlyProcessor processor,
            JdbcBatchItemWriter<MonthlyRankRow> weeklyMvUpsertWriter
    ) {
        return new StepBuilder("rankMonthlyMvStep", jobRepository)
                .<MonthlyAggRow, MonthlyRankRow>chunk(1000, transactionManager)
                .reader(weeklyAggReader)
                .processor(processor)
                .writer(weeklyMvUpsertWriter)
                .listener(stepMonitorListener)
                .build();
    }
}
