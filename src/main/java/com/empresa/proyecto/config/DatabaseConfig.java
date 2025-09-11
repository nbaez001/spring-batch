package com.empresa.proyecto.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Primary
    @Bean(name = "batchDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource batchDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.mysqldatasource")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "postgresDataSource")
    @ConfigurationProperties(prefix = "spring.postgresdatasource")
    public DataSource postgresDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean batchEntityManagerFactory(
            @Qualifier("batchDataSource") DataSource batchDataSource) {

        LocalContainerEntityManagerFactoryBean emf =
                new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(batchDataSource);
        emf.setPackagesToScan("com.empresa.proyecto.entity.batch");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return emf;
    }

    @Bean(name = "mysqlEntityManagerFactory")
    public EntityManagerFactory mysqlEntityManagerFactory(
            @Qualifier("mysqlDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean lem =
                new LocalContainerEntityManagerFactoryBean();
        lem.setDataSource(dataSource);
        lem.setPackagesToScan("com.empresa.proyecto.entity.mysql");
        lem.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        lem.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        lem.afterPropertiesSet();

        return lem.getObject();
    }

    @Bean(name = "postgresqlEntityManagerFactory")
    public EntityManagerFactory postgresqlEntityManagerFactory(
            @Qualifier("postgresDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean lem =
                new LocalContainerEntityManagerFactoryBean();
        lem.setDataSource(dataSource);
        lem.setPackagesToScan("com.empresa.proyecto.entity.postgresql");
        lem.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        lem.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        lem.afterPropertiesSet();

        return lem.getObject();
    }

    @Primary
    @Bean(name = "batchTransactionManager")
    public PlatformTransactionManager batchTransactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = "mysqlTransactionManager")
    public PlatformTransactionManager mysqlTransactionManager(
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = "postgresqlTransactionManager")
    public PlatformTransactionManager postgresqlTransactionManager(
            @Qualifier("postgresqlEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}