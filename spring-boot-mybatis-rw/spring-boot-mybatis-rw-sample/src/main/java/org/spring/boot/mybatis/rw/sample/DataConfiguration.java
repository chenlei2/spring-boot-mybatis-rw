package org.spring.boot.mybatis.rw.sample;

import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.spring.boot.mybatis.rw.starter.datasource.AbstractReadRoutingDataSource;
import org.spring.boot.mybatis.rw.starter.datasource.impl.RoundRobinRWRoutingDataSourceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(DataProperties.class)
public class DataConfiguration {
	
	@Autowired
	private DataProperties properties;

	@Bean(name="writeDataSource")
	@Primary
	public DataSource writeDataSource() {
		DataSource datasource = new DataSource();
		datasource.setUrl(properties.getWriteUrl());
		datasource.setDriverClassName(properties.getWriteDriverClassName());
		datasource.setUsername(properties.getWriteUsername());
		datasource.setPassword(properties.getWritePassword());
		return datasource;
	}

	@Bean(name = "readDataSource")
	public DataSource readOneDataSource() {
		DataSource datasource = new DataSource();
		datasource.setUrl(properties.getReadUrl());
		datasource.setDriverClassName(properties.getWriteDriverClassName());
		datasource.setUsername(properties.getReadUsername());
		datasource.setPassword(properties.getReadPassword());
		return datasource;
	}
	
    @Bean(name="readDataSources")  
    public List<DataSource> readDataSources(){  
        List<DataSource> dataSources = new ArrayList<DataSource>();  
        dataSources.add(readOneDataSource());   
        return dataSources;  
    } 
    
	/*@Bean //实现自己的读库负责均衡
	public AbstractReadRoutingDataSource roundRobinDataSouceProxy(@Qualifier("readDataSources")Object readDataSoures, @Qualifier("writeDataSource")Object writeDataSource) {
		RoundRobinRWRoutingDataSourceProxy proxy = new RoundRobinRWRoutingDataSourceProxy();
		proxy.setReadDataSoures((List<Object>)(readDataSoures));
		proxy.setWriteDataSource(writeDataSource);
		return proxy;
	}*/
   
}
