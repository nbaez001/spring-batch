package com.empresa.proyecto.config;

import com.empresa.proyecto.dto.DeceasedCsv;
import com.empresa.proyecto.entity.Deceased;
import com.empresa.proyecto.listener.SkipListenerImpl;
import com.empresa.proyecto.processor.DeceasedItemProcessor;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

@Configuration
public class JobConfig {

    @Autowired
    @Qualifier("deceasedEntityManagerFactory")
    private EntityManagerFactory deceasedEntityManagerFactory;

    @Autowired
    @Qualifier("deceasedTransactionManager")
    private PlatformTransactionManager deceasedTransactionManager;

    @Autowired
    private DeceasedItemProcessor deceasedItemProcessor;

    @Autowired
    private SkipListenerImpl skipListenerImpl;

    @Bean
    public Job deceasedJob(JobRepository jobRepository, Step deceasedStep) {
        return new JobBuilder("deceasedJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(deceasedStep)
                .build();
    }

    @Bean
    public Step deceasedStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             FlatFileItemReader<DeceasedCsv> flatFileItemReader,
                             JpaItemWriter<Deceased> jpaItemWriter) {
        return new StepBuilder("deceasedStep", jobRepository)
                .<DeceasedCsv, Deceased>chunk(50, transactionManager)
                .reader(flatFileItemReader)
                .processor(deceasedItemProcessor)
                .writer(jpaItemWriter)
                .faultTolerant()
                .skip(Throwable.class)
                .skipLimit(100)
                .retryLimit(1)
                .retry(Throwable.class)
                .listener(skipListenerImpl)
                .transactionManager(deceasedTransactionManager)
                .build();
    }

    @Bean
    public FlatFileItemReader<DeceasedCsv> deceasedCsvReader() {
        FlatFileItemReader<DeceasedCsv> flatFileItemReader =
                new FlatFileItemReader<>();
        flatFileItemReader.setResource(new FileSystemResource(
                new File("C:/nerio/proyectos/spring-batch/input-files/deceased.csv")));

        DefaultLineMapper<DeceasedCsv> lineMapper = new DefaultLineMapper<>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setNames("nationalId", "firstName", "paternalLastName", "maternalLastName",
                                "deathDate", "age", "genre","deathPlace", "source", "cause", "geoCode", "marital");
                    }
                });

                setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(DeceasedCsv.class);
                    }
                });
            }
        };

        flatFileItemReader.setLineMapper(lineMapper);
        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;
    }

    @Bean
    public JpaItemWriter<Deceased> jpaItemWriter() {
        JpaItemWriter<Deceased> jpaItemWriter =
                new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(deceasedEntityManagerFactory);
        return jpaItemWriter;
    }
}
