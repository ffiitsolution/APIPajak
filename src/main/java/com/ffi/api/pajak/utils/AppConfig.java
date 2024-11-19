package com.ffi.api.pajak.utils;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@PropertySource("file:C:/API_CONFIG/boffi.properties")
public class AppConfig {
    @Autowired
    private Environment env;
    
    public String get(String key) {
        String value = "";
        if (env != null) {
            value = env.getProperty(key);
        }
        return value == null ? "" : value;
    }
    
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(get("spring.datasource.url"));
        ds.setUsername(get("spring.datasource.username"));
        ds.setPassword(get("spring.datasource.password"));
        ds.setDriverClassName(get("spring.datasource.driverClassName"));
        return ds;
    }
    
    public String getOutletCode() {
        return get("app.outletCode");
    }
}
