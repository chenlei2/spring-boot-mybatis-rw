package org.spring.boot.mybatis.rw.starter.datasource;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.NamedThreadLocal;

public class ConnectionHold {
	/**
	 * 当前数据库链接是读还是写
	 */
	public final static ThreadLocal<String> CURRENT_CONNECTION = new NamedThreadLocal<String>("routingdatasource's key"){
		protected String initialValue() {
			return WRITE;
		};
	};
	/**
	 * 当前线程所有数据库链接
	 */
	public final static ThreadLocal<Map<String, Connection>> CONNECTION_CONTEXT = new NamedThreadLocal<Map<String, Connection>>(
			"connection map"){
		protected Map<String,Connection> initialValue() {
			return new HashMap<String,Connection>();
		};
	};
	/**
	 * 强制写数据源
	 */
	public final static ThreadLocal<Boolean> FORCE_WRITE = new NamedThreadLocal<Boolean>("FORCE_WRITE");

	public static final String READ = "read";
	public static final String WRITE = "write";

}
