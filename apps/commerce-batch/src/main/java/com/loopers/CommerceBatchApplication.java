package com.loopers;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.TimeZone;

@ConfigurationPropertiesScan
@SpringBootApplication
public class CommerceBatchApplication {

    @PostConstruct
    public void started() {
        // set timezone
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        int exitCode = SpringApplication.exit(SpringApplication.run(CommerceBatchApplication.class, args));
        System.exit(exitCode);
    }

    @Bean
    public static BeanFactoryPostProcessor dataSourceAliasForBatch() {
        return (ConfigurableListableBeanFactory beanFactory) -> {
            String[] names = beanFactory.getBeanNamesForType(DataSource.class);
            // DataSource가 1개면 그걸 "dataSource"라는 이름으로도 접근 가능하게 별칭 등록
            if (names.length == 1 && beanFactory instanceof DefaultListableBeanFactory dlbf) {
                if (!dlbf.containsBean("dataSource") && !dlbf.isAlias("dataSource")) {
                    dlbf.registerAlias(names[0], "dataSource");
                }
            }
        };
    }
}
