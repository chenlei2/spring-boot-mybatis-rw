package org.spring.boot.mybatis.rw.starter.datasource;

import org.springframework.core.NamedThreadLocal;

public class DataSourceHold {
	/**
	 * 当前数据组
	 */
	public final static ThreadLocal<String> CURRENT_DATASOURCE = new NamedThreadLocal<String>("routingdatasource's key");
	
}
