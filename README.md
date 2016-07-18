# spring-boot-mybatis-rw
基于mybatis，springboot开箱即用的读写分离插件

# Quick Start

介绍
---
此插件由以下3部分组成
- datasource：读写数据源的代理，支持一写多读，用户只需实现 org.spring.boot.mybatis.rw.starter.datasource.AbstractReadRoutingDataSource这个类，实现自己读数据源的负载均衡算法
- transaction：读写数据源的事务处理
- pulgin：mybatis插件实现读写路由

配置
---
- datasource：
```
<!--简单的一个master和多个slaver 读写分离的数据源 -->
	<bean id="dataSource" 
	    class="org.spring.boot.mybatis.rw.starter.datasource.impl.RoundRobinRWRoutingDataSourceProxy">
	    <property name="writeDataSource" ref="writeDS"/>
	    <property name="readDataSoures">
	        <list>
	            <ref bean="readDS"/>
	            <ref bean="readDS"/>
	            <ref bean="readDS"/>
	        </list>
	    </property>
	</bean>
``` 
- transaction：
``` 
<!--自定义事务工厂  -->
	<bean id="transactionFactory" class="org.spring.boot.mybatis.rw.starter.transaction.RWManagedTransactionFactory"/>
	<!-- mybatis配置 -->
	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
		<property name="dataSource" ref="dataSource" />
		<property name="transactionFactory" ref="transactionFactory"/>
		<property name="configLocation" value="classpath:mybatis-plugin-config.xml" />
		<!-- mapper和resultmap配置路径 -->
		<property name="mapperLocations">
			<list>
				<value>classpath:org.spring.boot.mybatis.rw.sample.mapper/**/*Mapper.xml
				</value>
			</list>
		</property> 
	</bean>
``` 

总结
---
只需将数据源和事务工厂注入到sqlSessionFactory中，其他配置不变，便实现读写分离，对代码0入侵，配置简单，非常方便老项目的迁移。
[详细配置](https://github.com/chenlei2/spring-boot-mybatis-rw/blob/master/spring-boot-mybatis-rw/mybatis-rw-sample-xml/src/main/resources/spring-mybatis.xml)
