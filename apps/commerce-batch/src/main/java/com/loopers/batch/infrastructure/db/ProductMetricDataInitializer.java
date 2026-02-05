package com.loopers.batch.infrastructure.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class ProductMetricDataInitializer implements CommandLineRunner {
    private final DataSource dataSource;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Override
    public void run(String... args) throws Exception {

        if(ddlAuto.equals("none")) {
            log.info("[ProductMetricDataInitializer] skipped because ddl-auto is none");
            return;
        }


        log.info("[ProductMetricDataInitializer] start");
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(
                    conn,
                    new ClassPathResource("db/fixtures/.sql")
            );
        }
        log.info("[ProductMetricDataInitializer] done");
    }
}
