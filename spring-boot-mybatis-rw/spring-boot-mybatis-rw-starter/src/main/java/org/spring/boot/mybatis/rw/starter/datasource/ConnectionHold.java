package org.spring.boot.mybatis.rw.starter.datasource;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.NamedThreadLocal;

public class ConnectionHold {
	
	public final static ThreadLocal<String> CURRENT_DATASOURCE = new NamedThreadLocal<String>("routingdatasource's key"){
		protected String initialValue() {
			return WRITE;
		};
	};
	public final static ThreadLocal<Map<String, Connection>> CONNECTION_CONTEXT = new NamedThreadLocal<Map<String, Connection>>(
			"connection map"){
		protected Map<String,Connection> initialValue() {
			return new HashMap<String,Connection>();
		};
	};
	public final static ThreadLocal<Boolean> FORCE_WRITE = new NamedThreadLocal<Boolean>("FORCE_WRITE");

	public static final String READ = "read";
	public static final String WRITE = "write";

}
