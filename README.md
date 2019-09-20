# spring-boot-mybatis-rw
基于mybatis，springboot开箱即用的读写分离插件

# Quick Start
Maven dependency
---
```
<dependency>
  <groupId>com.github.chenlei2</groupId>
  <artifactId>spring-boot-mybatis-rw-starter</artifactId>
  <version>0.0.2</version>
</dependency>
```

介绍
---
此插件由以下2部分组成
- datasource：读写数据源的代理，支持一写多读，用户只需实现 com.github.chenlei2.springboot.mybatis.rw.starter.datasource.AbstractReadRoutingDataSource这个类，实现自己读数据源的负载均衡算法，默认实现com.github.chenlei2.springboot.mybatis.rw.starter.datasource.impl.RoundRobinRWDataSourceRout
- pulgin：mybatis插件实现读写路由


@Transactional(isolation = Isolation.SERIALIZABLE) 强制本事务都走写库，插件会把事务隔离级别修改为默认隔离级别

spring-boot 配置
---
```
spring.mybatis.rw.readDataSources[0].url=jdbc:MySql://localhost:3306/test?characterEncoding=UTF-8
spring.mybatis.rw.readDataSources[0].driverClassName=com.mysql.jdbc.Driver
spring.mybatis.rw.readDataSources[0].username=root
spring.mybatis.rw.readDataSources[0].password=123456
spring.mybatis.rw.readDataSources[1].url=jdbc:MySql://localhost:3306/test?characterEncoding=UTF-8
spring.mybatis.rw.readDataSources[1].driverClassName=com.mysql.jdbc.Driver
spring.mybatis.rw.readDataSources[1].username=root
spring.mybatis.rw.readDataSources[1].password=123456

spring.mybatis.rw.writeDataSource.url=jdbc:MySql://localhost:3306/chenlei?characterEncoding=UTF-8
spring.mybatis.rw.writeDataSource.driverClassName=com.mysql.jdbc.Driver
spring.mybatis.rw.writeDataSource.username=root
spring.mybatis.rw.writeDataSource.password=123456
``` 

XML配置
---
- datasource：
```
<!--简单的一个master和多个slaver 读写分离的数据源 -->
	<bean id="roundRobinRWDataSourceRout"
	    class="com.github.chenlei2.springboot.mybatis.rw.starter.datasource.impl.RoundRobinRWDataSourceRout">
	    <property name="writeDataSource" ref="writeDS"/>
	    <property name="readDataSoures">
	        <list>
	            <ref bean="readDS"/>
	            <ref bean="readDS"/>
	            <ref bean="readDS"/>
	        </list>
	    </property>
	</bean>
	<bean id="dataSource" class="com.github.chenlei2.springboot.mybatis.rw.starter.datasource.DataSourceProxy">
		<constructor-arg ref="roundRobinRWDataSourceRout"/>
	</bean>
``` 

总结
---
只需替换数据源，其他配置不变，便实现读写分离，对代码0入侵，配置简单，非常方便老项目的迁移。
[详细配置](https://github.com/chenlei2/spring-boot-mybatis-rw/blob/master/spring-boot-mybatis-rw/mybatis-rw-sample-xml/src/main/resources/spring-mybatis.xml)
