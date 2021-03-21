package com.miro.sample.board.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Profile("h2")
@Configuration
@EnableJpaAuditing
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "datasource")
    public DataSource getDataSource() {
        return DataSourceBuilder.create().build();
    }
}
