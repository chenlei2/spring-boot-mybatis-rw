package org.spring.boot.mybatis.rw.sample;

import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataBaseConfiguration {

	@Bean(name="writeDataSource")
	public DataSource writeDataSource() {
		DataSource datasource = new DataSource();
		datasource.setUrl("jdbc:MySql://localhost:3306/test");
		datasource.setDriverClassName("com.mysql.jdbc.Driver");
		datasource.setUsername("root");
		datasource.setPassword("123456");
		return datasource;
	}

	@Bean(name = "readDataSource")
	public DataSource readOneDataSource() {
		DataSource datasource = new DataSource();
		datasource.setUrl("jdbc:MySql://localhost:3306/chenlei?characterEncoding=UTF-8");
		datasource.setDriverClassName("com.mysql.jdbc.Driver");
		datasource.setUsername("root");
		datasource.setPassword("123456");
		return datasource;
	}
	
    @Bean(name="readDataSources")  
    public List<DataSource> readDataSources(){  
        List<DataSource> dataSources = new ArrayList<DataSource>();  
        dataSources.add(readOneDataSource());   
        return dataSources;  
    } 
}
