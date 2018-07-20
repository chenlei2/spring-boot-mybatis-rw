package pres.chenlei2.springboot.mybatis.rw.starter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import pres.chenlei2.springboot.mybatis.rw.starter.datasource.DataSourceProxy;
import pres.chenlei2.springboot.mybatis.rw.starter.datasource.DataSourceRout;
import pres.chenlei2.springboot.mybatis.rw.starter.datasource.impl.RoundRobinRWDataSourceRout;
import pres.chenlei2.springboot.mybatis.rw.starter.pulgin.RWPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 *
 * @author chenlei
 */
@Configuration()
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@EnableConfigurationProperties(MybatisProperties.class)
@ConfigurationProperties("spring.mybatis.rw")
public class MybatisAutoConfiguration {

	@Autowired
	private MybatisProperties properties;

	@Autowired(required = false)
	private Interceptor[] interceptors;
	@Autowired
	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	@Autowired(required = false)
	private DatabaseIdProvider databaseIdProvider;
	
	private List<DataSource> readDataSources = new ArrayList<>();

	@PostConstruct
	public void checkConfigFileExists() {
		if (this.properties.isCheckConfigLocation() && StringUtils.hasText(this.properties.getConfigLocation())) {
			Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
			Assert.state(resource.exists(), "Cannot find config location: " + resource
					+ " (please add config file or check your Mybatis configuration)");
		}
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSourceProxy dataSource)
			throws Exception {

		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		Interceptor rwplugin = new RWPlugin();
		if (StringUtils.hasText(this.properties.getConfigLocation())) {
			factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
		}
		factory.setConfiguration(properties.getConfiguration());

		if (ObjectUtils.isEmpty(this.interceptors)) {
			Interceptor[] plugins = { rwplugin };
			factory.setPlugins(plugins);
		} else {
			List<Interceptor> interceptorList = Arrays.asList(interceptors);
			interceptorList.add(rwplugin);
			factory.setPlugins((Interceptor[]) interceptorList.toArray());
		}
		if (this.databaseIdProvider != null) {
			factory.setDatabaseIdProvider(this.databaseIdProvider);
		}
		if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
			factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
		}
		if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
			factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
		}
		if (!ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {
			factory.setMapperLocations(this.properties.resolveMapperLocations());
		}
		factory.setDataSource(dataSource);
		return factory.getObject();
	}


	@Bean
	@ConditionalOnMissingBean
	public DataSourceRout readRoutingDataSource() {
		RoundRobinRWDataSourceRout proxy = new RoundRobinRWDataSourceRout();
		proxy.setReadDataSoures(getReadDataSources());
		proxy.setWriteDataSource(writeDataSource());
		return proxy;
	}

	
	
	public List<DataSource> getReadDataSources() {
		return readDataSources;
	}
	@ConfigurationProperties("spring.mybatis.rw.writeDataSource")
	@Bean
	public DataSource writeDataSource() {
		return new DataSource();
	}

	@Bean
	public DataSourceProxy dataSource(DataSourceRout dataSourceRout) {
		return new DataSourceProxy(dataSourceRout);
	}

	@Bean
	public DataSourceTransactionManager transactionManager(DataSourceProxy DataSource) {
		return new DataSourceTransactionManager(DataSource);
	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		ExecutorType executorType = this.properties.getExecutorType();
		if (executorType != null) {
			return new SqlSessionTemplate(sqlSessionFactory, executorType);
		} else {
			return new SqlSessionTemplate(sqlSessionFactory);
		}
	}

}
