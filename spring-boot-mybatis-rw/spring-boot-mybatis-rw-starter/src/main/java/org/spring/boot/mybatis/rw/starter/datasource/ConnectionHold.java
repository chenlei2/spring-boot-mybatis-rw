package org.spring.boot.mybatis.rw.starter.datasource;

import java.sql.Connection;
import java.util.Map;

import org.springframework.core.NamedThreadLocal;

public class ConnectionHold {
	
	public final static ThreadLocal<String> currentDataSource = new NamedThreadLocal<String>("routingdatasource's key");
	public final static ThreadLocal<Map<String, Connection>> ConnectionContext = new NamedThreadLocal<Map<String, Connection>>(
			"connection map");
	public final static ThreadLocal<Boolean> FORCE_WRITE = new NamedThreadLocal<Boolean>("FORCE_WRITE");

	public static final String READ = "read";
	public static final String WRITE = "write";

}
