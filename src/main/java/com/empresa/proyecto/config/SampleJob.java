package com.empresa.proyecto.config;

import com.empresa.proyecto.model.StudentJdbc;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

@Configuration
public class SampleJob {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.universitydatasource")
    public DataSource universityDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public Job chunkJob(JobRepository jobRepository, Step firstChunkStep) {
        return new JobBuilder("chunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep)
                .build();
    }

    @Bean
    public Step firstChunkStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                               JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader,
                               FlatFileItemWriter<StudentJdbc> flatFileItemWriter) {
        return new StepBuilder("firstChunkStep", jobRepository)
                .<StudentJdbc, StudentJdbc>chunk(3, transactionManager)
                .reader(jdbcCursorItemReader)
                .writer(flatFileItemWriter)
                .build();
    }

    @Bean
    public JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader() {
        JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader =
                new JdbcCursorItemReader<>();
        jdbcCursorItemReader.setDataSource(universityDataSource());
        jdbcCursorItemReader.setSql("""
                SELECT id, first_name as firstname, last_name as lastName, email
                FROM university.student
                """);
        jdbcCursorItemReader.setRowMapper(new BeanPropertyRowMapper<>() {
            {
                setMappedClass(StudentJdbc.class);
            }
        });
        jdbcCursorItemReader.setCurrentItemCount(2);
        jdbcCursorItemReader.setMaxItemCount(8);

        return jdbcCursorItemReader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<StudentJdbc> flatFileItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource
    ) {
        FlatFileItemWriter<StudentJdbc> flatFileItemWriter =
                new FlatFileItemWriter<>();
        flatFileItemWriter.setResource(fileSystemResource);
        flatFileItemWriter.setHeaderCallback(
                writer -> writer.write("Id|First Name|Last Name|Email"));
        flatFileItemWriter.setLineAggregator(new DelimitedLineAggregator<>() {
            {
                setDelimiter("|");
                setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                    {
                        setNames(new String[]{"id", "firstName", "lastName", "email"});
                    }
                });
            }
        });
        flatFileItemWriter.setFooterCallback(writer ->
                writer.write("Created @ " + new Date()));

        return flatFileItemWriter;
    }
}
