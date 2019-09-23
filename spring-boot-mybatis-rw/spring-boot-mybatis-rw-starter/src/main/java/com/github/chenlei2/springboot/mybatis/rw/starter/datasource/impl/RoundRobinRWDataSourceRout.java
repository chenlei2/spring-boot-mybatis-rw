package com.github.chenlei2.springboot.mybatis.rw.starter.datasource.impl;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import com.github.chenlei2.springboot.mybatis.rw.starter.datasource.AbstractRWDataSourceRout;

/**
 * 
 * @author chenlei
 * 简单实现读数据源负载均衡
 *
 */
public class RoundRobinRWDataSourceRout extends AbstractRWDataSourceRout {

	@Override
	protected DataSource loadBalance() {
		Random random =  new Random();
		return getResolvedReadDataSources().get(random.nextInt(getReadDsSize()));
	}

}
