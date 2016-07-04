package org.spring.boot.mybatis.rw.starter.pulgin;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Properties;

import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.spring.boot.mybatis.rw.starter.datasource.AbstractRWRoutingDataSourceProxy;
import org.spring.boot.mybatis.rw.starter.util.ReflectionUtils;
import org.springframework.jdbc.datasource.ConnectionProxy;

/**
 * 不侵入mybatis的逻辑，实现读写分离
 * 
 * @author chenlei
 *
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}) })
public class RWPlugin implements Interceptor {

	public Object intercept(Invocation invocation) throws Throwable {

		Connection conn = (Connection) invocation.getArgs()[0];
		if (conn instanceof ConnectionProxy) {
			StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

			MappedStatement mappedStatement = null;
			if (statementHandler instanceof RoutingStatementHandler) {
				StatementHandler delegate = (StatementHandler) ReflectionUtils.getFieldValue(statementHandler,
						"delegate");
				mappedStatement = (MappedStatement) ReflectionUtils.getFieldValue(delegate, "mappedStatement");
			} else {
				mappedStatement = (MappedStatement) ReflectionUtils.getFieldValue(statementHandler, "mappedStatement");
			}
			String key = AbstractRWRoutingDataSourceProxy.WRITE;

			if (mappedStatement.getSqlCommandType() == SqlCommandType.SELECT) {
				key = AbstractRWRoutingDataSourceProxy.READ;
			} else {
				key = AbstractRWRoutingDataSourceProxy.WRITE;
			}
			AbstractRWRoutingDataSourceProxy.currentDataSource.set(key);
			
			if(AbstractRWRoutingDataSourceProxy.ConnectionContext.get() == null){
				AbstractRWRoutingDataSourceProxy.ConnectionContext.set(new HashMap<String, Connection>());
			}
			if (!AbstractRWRoutingDataSourceProxy.ConnectionContext.get().containsKey(key)) {
				ConnectionProxy conToUse = (ConnectionProxy) conn;
				conToUse.getTargetConnection();
			}
		}

		return invocation.proceed();

	}

	public Object plugin(Object target) {
		if (target instanceof StatementHandler) {  
            return Plugin.wrap(target, this);  
        } else {  
            return target;  
        }  
	}

	public void setProperties(Properties properties) {
		// NOOP

	}

}
