package com.empresa.proyecto.config;

import com.empresa.proyecto.model.StudentCsv;
import com.empresa.proyecto.model.StudentRequest;
import com.empresa.proyecto.processor.FirstItemProcessor;
import com.empresa.proyecto.service.StudentService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

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

    @Autowired
    private FirstItemProcessor firstItemProcessor;

    @Autowired
    private StudentService studentService;

    @Bean
    public Job chunkJob(JobRepository jobRepository, Step firstChunkStep) {
        return new JobBuilder("chunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep)
                .build();
    }

    @Bean
    public Step firstChunkStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                               FlatFileItemReader<StudentCsv> studentCsvReader,
                               ItemWriterAdapter<StudentRequest> itemWriterAdapter) {
        return new StepBuilder("firstChunkStep", jobRepository)
                .<StudentCsv, StudentRequest>chunk(3, transactionManager)
                .reader(studentCsvReader)
                .processor(firstItemProcessor)
                .writer(itemWriterAdapter)
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

        DefaultLineMapper<StudentCsv> lineMapper = new DefaultLineMapper<>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setNames("Id", "First Name", "Last Name", "Email");
                    }
                });

                setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(StudentCsv.class);
                    }
                });
            }
        };

        flatFileItemReader.setLineMapper(lineMapper);
        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;
    }

    @Bean
    public ItemWriterAdapter<StudentRequest> itemWriterAdapter() {
        ItemWriterAdapter<StudentRequest> itemWriterAdapter =
                new ItemWriterAdapter<>();
        itemWriterAdapter.setTargetObject(studentService);
        itemWriterAdapter.setTargetMethod("restCallToCreateStudents");

        return itemWriterAdapter;
    }
}
