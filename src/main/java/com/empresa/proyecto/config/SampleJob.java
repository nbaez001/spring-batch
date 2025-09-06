package com.empresa.proyecto.config;

import com.empresa.proyecto.listener.FirstJobListener;
import com.empresa.proyecto.listener.FirstStepListener;
import com.empresa.proyecto.processor.FirstItemProcessor;
import com.empresa.proyecto.reader.FirstItemReader;
import com.empresa.proyecto.service.SecondTasklet;
import com.empresa.proyecto.writer.FirstItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SampleJob {

    @Autowired
    private SecondTasklet secondTasklet;

    @Autowired
    private FirstJobListener firstJobListener;

    @Autowired
    private FirstStepListener firstStepListener;

    @Autowired
    private FirstItemReader firstItemReader;

    @Autowired
    private FirstItemProcessor firstItemProcessor;

    @Autowired
    private FirstItemWriter firstItemWriter;

    @Bean
    public Job firstJob(JobRepository jobRepository, Step firstStep, Step secondStep) {
        return new JobBuilder("firstJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(firstStep)
                .next(secondStep)
                .listener(firstJobListener)
                .build();
    }

    @Bean
    public Step firstStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("firstStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("This is the first tasklet step");
                    System.out.println("SEC = " + chunkContext.getStepContext().getStepExecutionContext());
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(firstStepListener)
                .build();
    }

    @Bean
    public Step secondStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("secondStep", jobRepository)
                .tasklet(secondTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job secondJob(JobRepository jobRepository, Step firstChunkStep, Step thirdStep) {
        return new JobBuilder("secondJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep)
                .next(thirdStep)
                .build();
    }

    @Bean
    public Step firstChunkStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("firstChunkStep", jobRepository)
                .<Integer, Long>chunk(3, transactionManager)
                .reader(firstItemReader)
                .processor(firstItemProcessor)
                .writer(firstItemWriter)
                .build();
    }

    @Bean
    public Step thirdStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("thirdStep", jobRepository)
                .tasklet(secondTasklet, transactionManager)
                .build();
    }

}
