package com.university.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.datasource.pooled.PooledDataSource;

import java.io.Reader;

@ApplicationScoped
public class MyBatisConfig {

    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void init() {
        try {
            // Create a simple DataSource with the same connection info as JPA
            PooledDataSource dataSource = new PooledDataSource();
            dataSource.setDriver("org.postgresql.Driver");
            dataSource.setUrl("jdbc:postgresql://localhost:5432/university");
            dataSource.setUsername("uniuser");
            dataSource.setPassword("unipass");

            // Create configuration programmatically
            Configuration configuration = new Configuration();

            // Set up environment with the DataSource
            Environment environment = new Environment(
                    "development",
                    new ManagedTransactionFactory(),
                    dataSource
            );
            configuration.setEnvironment(environment);

            // Add only Faculty mapper (Course and Student use JPA)
            configuration.addMapper(com.university.mybatis.mapper.FacultyMapper.class);

            // Parse and add only the Faculty XML mapper
            addXmlMapper(configuration, "FacultyMapper.xml");

            // Build SqlSessionFactory
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        } catch (Exception e) {
            throw new RuntimeException("Error initializing MyBatis SqlSessionFactory", e);
        }
    }

    private void addXmlMapper(Configuration configuration, String resource) {
        try {
            Reader reader = Resources.getResourceAsReader(resource);
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(reader, configuration, resource, configuration.getSqlFragments());
            mapperParser.parse();
        } catch (Exception e) {
            System.err.println("Could not load mapper: " + resource + " - " + e.getMessage());
        }
    }

    @Produces
    @Named("sqlSessionFactory")
    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
}