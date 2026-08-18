package com.manisha.manishamart.listener;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

@WebListener
public class DataSourceListener implements ServletContextListener {

    private static HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:./data/manishamart");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);

        dataSource = new HikariDataSource(config);
        sce.getServletContext().setAttribute("dataSource", dataSource);

        runSchema();
    }

    private void runSchema() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             InputStream is = getClass().getResourceAsStream("/schema.sql")) {

            if (is == null) {
                System.err.println("schema.sql not found on classpath");
                return;
            }

            StringBuilder sql = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sql.append(line).append("\n");
                }
            }

            for (String statement : sql.toString().split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
            System.out.println("Schema applied successfully.");

        } catch (Exception e) {
            System.err.println("Failed to apply schema: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
