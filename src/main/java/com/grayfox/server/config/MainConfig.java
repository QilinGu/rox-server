package com.grayfox.server.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.scheduling.annotation.EnableAsync;

import com.foursquare4j.FoursquareApi;
import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Import({MainConfig.DataSourceConfig.class, MainConfig.BeanConfig.class})
public class MainConfig {

    @Bean
    public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer(ResourceLoader resourceLoader) throws IOException {
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        ppc.setProperties(configsProperties(resourceLoader).getObject());
        return ppc;
    }

    @Bean
    public static PropertiesFactoryBean configsProperties(ResourceLoader resourceLoader) throws IOException {
        PropertiesFactoryBean props = new PropertiesFactoryBean();
        props.setLocation(resourceLoader.getResource("/WEB-INF/resources/configs.properties"));
        props.afterPropertiesSet();
        return props;
    }

    @EnableAsync
    @Configuration
    @EnableTransactionManagement
    @ComponentScan(basePackages = { 
            "com.grayfox.server.dao.jdbc",
            "com.grayfox.server.datasource",
            "com.grayfox.server.service",
            "com.grayfox.server.ws.rest"})
    public static class BeanConfig {

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public FoursquareApi foursquareApi(
                @Value("${foursquare.app.client.id}") String clientId, 
                @Value("${foursquare.app.client.secret}") String clientSecret) {
            return new FoursquareApi(clientId, clientSecret);
        }

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public GeoApiContext geoApiContext(@Value("${google.api.key}") String apiKey) {
            return new GeoApiContext().setApiKey(apiKey);
        }

        @Bean
        public DataSourceTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Configuration
    public static class DataSourceConfig {

        @Bean
        public DataSource dataSource(
                @Value("${jdbc.driver.class}") String driverClass,
                @Value("${jdbc.url}") String url,
                @Value("${jdbc.user}") String user,
                @Value("${jdbc.password}") String password) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(driverClass);
            dataSource.setUrl(url);
            dataSource.setUsername(user);
            dataSource.setPassword(password);
            return dataSource;
        }
    }
}