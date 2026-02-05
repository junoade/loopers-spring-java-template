package com.loopers.batch.job.rankWeekly;

import com.loopers.batch.job.rankWeekly.step.RankWeeklyProcessor;
import com.loopers.batch.job.rankWeekly.step.dto.WeeklyAggRow;
import com.loopers.batch.job.rankWeekly.step.dto.WeeklyRankRow;
import com.loopers.batch.listener.JobListener;
import com.loopers.batch.listener.StepMonitorListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = "rankWeeklyMvJob")
@Configuration
// @EnableBatchProcessing
@RequiredArgsConstructor
public class RankWeeklyJobConfig {
    private final JobListener jobListener;
    private final StepMonitorListener stepMonitorListener;

    @Bean
    public Job rankWeeklyMvJob(JobRepository jobRepository, @Qualifier("rankWeeklyMvStep") Step rankWeeklyMvStep) {
        return new JobBuilder("rankWeeklyMvJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(rankWeeklyMvStep)
                .listener(jobListener)
                .build();
    }

    @Bean
    public Step rankWeeklyMvStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcPagingItemReader<WeeklyAggRow> weeklyAggReader,
            RankWeeklyProcessor processor,
            JdbcBatchItemWriter<WeeklyRankRow> weeklyMvUpsertWriter
    ) {
        return new StepBuilder("rankWeeklyMvStep", jobRepository)
                .<WeeklyAggRow, WeeklyRankRow>chunk(1000, transactionManager)
                .reader(weeklyAggReader)
                .processor(processor)
                .writer(weeklyMvUpsertWriter)
                .listener(stepMonitorListener)
                .build();
    }
}
