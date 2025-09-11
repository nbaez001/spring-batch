package com.empresa.proyecto.config;

import com.empresa.proyecto.entity.mysql.StudentT;
import com.empresa.proyecto.entity.postgresql.Student;
import com.empresa.proyecto.listener.SkipListenerImpl;
import com.empresa.proyecto.processor.FirstItemProcessor;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SampleJob {

    @Autowired
    @Qualifier("mysqlEntityManagerFactory")
    private EntityManagerFactory mysqlEntityManagerFactory;

    @Autowired
    @Qualifier("postgresqlEntityManagerFactory")
    private EntityManagerFactory postgresqlEntityManagerFactory;

    @Autowired
    @Qualifier("mysqlTransactionManager")
    private PlatformTransactionManager mysqlTransactionManager;

    @Autowired
    private FirstItemProcessor firstItemProcessor;

    @Autowired
    private SkipListenerImpl skipListenerImpl;

    @Bean
    public Job chunkJob(JobRepository jobRepository, Step firstChunkStep) {
        return new JobBuilder("chunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep)
                .build();
    }

    @Bean
    public Step firstChunkStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               JpaCursorItemReader<Student> jpaCursorItemReader,
                               JpaItemWriter<StudentT> jpaItemWriter) {
        return new StepBuilder("firstChunkStep", jobRepository)
                .<Student, StudentT>chunk(3, transactionManager)
                .reader(jpaCursorItemReader)
                .processor(firstItemProcessor)
                .writer(jpaItemWriter)
                .faultTolerant()
                .skip(Throwable.class)
                .skipLimit(100)
                .retryLimit(1)
                .retry(Throwable.class)
                .listener(skipListenerImpl)
                .transactionManager(mysqlTransactionManager)
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<Student> jpaCursorItemReader(
            @Value("#{jobParameters['currentItemCount']}") Integer currentItemCount,
            @Value("#{jobParameters['maxItemCount']}") Integer maxItemCount
    ) {
        JpaCursorItemReader<Student> jpaCursorItemReader =
                new JpaCursorItemReader<>();
        jpaCursorItemReader.setEntityManagerFactory(postgresqlEntityManagerFactory);
        jpaCursorItemReader.setQueryString("From Student");
        jpaCursorItemReader.setCurrentItemCount(currentItemCount);
        jpaCursorItemReader.setMaxItemCount(maxItemCount);

        return jpaCursorItemReader;
    }

    @Bean
    public JpaItemWriter<StudentT> jpaItemWriter() {
        JpaItemWriter<StudentT> jpaItemWriter =
                new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(mysqlEntityManagerFactory);
        return jpaItemWriter;
    }
}
