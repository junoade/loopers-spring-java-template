package com.loopers.batch.job.rankWeekly;

import com.loopers.batch.job.rankWeekly.step.RankWeeklyProcessor;
import com.loopers.batch.job.rankWeekly.step.dto.WeeklyAggRow;
import com.loopers.batch.job.rankWeekly.step.dto.WeeklyRankRow;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;

@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = "rankWeeklyMvJob")
@Configuration
// @EnableBatchProcessing
@RequiredArgsConstructor
public class RankWeeklyJobConfig {
    @Bean
    public Job rankWeeklyMvJob(JobRepository jobRepository, @Qualifier("rankWeeklyMvStep") Step rankWeeklyMvStep) {
        return new JobBuilder("rankWeeklyMvJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(rankWeeklyMvStep)
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
                .listener(rankWeeklyStepLoggingListener())
                .build();
    }

    @Bean
    public StepExecutionListener rankWeeklyStepLoggingListener() {
        return new StepExecutionListenerSupport() {
            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                // 빠르게 "COMPLETED"가 뜨는데 DB에 적재가 안 되는 경우 대부분 readCount=0 입니다.
                System.out.printf(
                        "[RankWeekly] step=%s status=%s read=%d write=%d commit=%d rollback=%d filter=%d skip=%d\n",
                        stepExecution.getStepName(),
                        stepExecution.getStatus(),
                        stepExecution.getReadCount(),
                        stepExecution.getWriteCount(),
                        stepExecution.getCommitCount(),
                        stepExecution.getRollbackCount(),
                        stepExecution.getFilterCount(),
                        stepExecution.getSkipCount()
                );
                return ExitStatus.COMPLETED;
            }
        };
    }
}
