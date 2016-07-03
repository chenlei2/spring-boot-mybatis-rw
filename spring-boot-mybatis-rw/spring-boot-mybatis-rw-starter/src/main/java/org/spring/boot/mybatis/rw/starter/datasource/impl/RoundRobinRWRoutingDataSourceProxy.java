package org.spring.boot.mybatis.rw.starter.datasource.impl;

import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.spring.boot.mybatis.rw.starter.datasource.AbstractRWRoutingDataSourceProxy;
/**
 * 
 * @author chenlei
 * 简单实现读数据源负载均衡
 *
 */
public class RoundRobinRWRoutingDataSourceProxy extends AbstractRWRoutingDataSourceProxy {

	private AtomicInteger count = new AtomicInteger(0);

	@Override
	protected DataSource loadBalance() {
		int index = Math.abs(count.incrementAndGet()) % getReadDsSize();
		return getResolvedReadDataSources().get(index);
	}

}
