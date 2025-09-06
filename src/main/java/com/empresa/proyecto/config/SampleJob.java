package com.empresa.proyecto.config;

import com.empresa.proyecto.model.StudentCsv;
import com.empresa.proyecto.writer.FirstItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SampleJob {

    @Autowired
    private FirstItemWriter firstItemWriter;

    @Bean
    public Job chunkJob(JobRepository jobRepository, Step firstChunkStep) {
        return new JobBuilder("chunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep)
                .build();
    }

    @Bean
    public Step firstChunkStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                               FlatFileItemReader<StudentCsv> studentCsvReader) {
        return new StepBuilder("firstChunkStep", jobRepository)
                .<StudentCsv, StudentCsv>chunk(3, transactionManager)
                .reader(studentCsvReader)
                .writer(firstItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<StudentCsv> studentCsvReader(
            @Value("#{jobParameters['inputFile']}") FileSystemResource fileSystemResource
    ) {
        FlatFileItemReader<StudentCsv> flatFileItemReader =
                new FlatFileItemReader<>();
        flatFileItemReader.setResource(fileSystemResource);

        DefaultLineMapper<StudentCsv> lineMapper =
                new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer =
                new DelimitedLineTokenizer();
        lineTokenizer.setNames("Id", "First Name", "Last Name", "Email");
        lineTokenizer.setDelimiter("|");

        BeanWrapperFieldSetMapper<StudentCsv> fieldSetMapper =
                new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(StudentCsv.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        flatFileItemReader.setLineMapper(lineMapper);
        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;
    }

}
