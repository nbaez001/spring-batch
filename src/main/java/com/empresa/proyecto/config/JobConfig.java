package com.empresa.proyecto.config;

import com.empresa.proyecto.dto.DeceasedCsv;
import com.empresa.proyecto.entity.Deceased;
import com.empresa.proyecto.listener.FileLoggingListener;
import com.empresa.proyecto.listener.SkipListenerImpl;
import com.empresa.proyecto.processor.DeceasedItemProcessor;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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

    @Autowired
    private FileLoggingListener fileLoggingListener;

    @Value("${props.pending-dir}")
    private String pendingDir;

    @Value("${props.processed-dir}")
    private String processedDir;

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
                             MultiResourceItemReader<DeceasedCsv> multiResourceItemReader,
                             JpaItemWriter<Deceased> jpaItemWriter) {
        return new StepBuilder("deceasedStep", jobRepository)
                .<DeceasedCsv, Deceased>chunk(50, transactionManager)
                .reader(multiResourceItemReader)
                .processor(deceasedItemProcessor)
                .writer(jpaItemWriter)
                .faultTolerant()
                .skip(Throwable.class)
                .skipLimit(100)
                .retryLimit(1)
                .retry(Throwable.class)
                .listener(skipListenerImpl)
                .listener(fileMoveListener())
                .listener(fileLoggingListener)
                .transactionManager(deceasedTransactionManager)
                .build();
    }

    @Bean
    public MultiResourceItemReader<DeceasedCsv> multiResourceItemReader(FlatFileItemReader<DeceasedCsv> deceasedCsvReader) {
        MultiResourceItemReader<DeceasedCsv> reader = new MultiResourceItemReader<>();
        File folder = new File(pendingDir);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        Resource[] resources = new Resource[files != null ? files.length : 0];

        for (int i = 0; i < resources.length; i++) {
            resources[i] = new FileSystemResource(files[i]);
        }

        reader.setResources(resources);
        reader.setDelegate(deceasedCsvReader);
        return reader;
    }

    @Bean
    public FlatFileItemReader<DeceasedCsv> deceasedCsvReader() {
        FlatFileItemReader<DeceasedCsv> flatFileItemReader =
                new FlatFileItemReader<>();

        DefaultLineMapper<DeceasedCsv> lineMapper = new DefaultLineMapper<>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setNames("nationalId", "firstName", "paternalLastName", "maternalLastName",
                                "deathDate", "age", "genre", "deathPlace", "source", "cause", "geoCode", "marital");
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

    @Bean
    public StepExecutionListener fileMoveListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
            }

            @Override
            public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
                File pendingDirFile = new File(pendingDir);
                File[] files = pendingDirFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

                if (files != null) {
                    for (File file : files) {
                        try {
                            Files.move(file.toPath(),
                                    new File(processedDir, file.getName()).toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return stepExecution.getExitStatus();
            }
        };
    }
}
