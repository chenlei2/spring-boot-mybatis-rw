package org.spring.boot.mybatis.rw.starter.pulgin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Properties;

import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.spring.boot.mybatis.rw.starter.datasource.ConnectionHold;
import org.springframework.jdbc.datasource.ConnectionProxy;

/**
 * 不侵入mybatis的逻辑，实现读写分离
 * 
 * @author chenlei
 *
 */
@Deprecated
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}) })
public class LazyConnectionRWPlugin implements Interceptor {

	public Object intercept(Invocation invocation) throws Throwable {

		Connection conn = (Connection) invocation.getArgs()[0];
		conn = unwrapConnection(conn);
		if (conn instanceof ConnectionProxy) {			
			//强制走写库
			if(ConnectionHold.FORCE_WRITE.get() != null && ConnectionHold.FORCE_WRITE.get()){
				routeConnection(ConnectionHold.WRITE, conn);
				return invocation.proceed();
			}	
			StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
			MetaObject metaObject = MetaObject.forObject(statementHandler, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
			MappedStatement mappedStatement = null;
			if (statementHandler instanceof RoutingStatementHandler) {
				mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
			} else {
				mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
			}
			String key = ConnectionHold.WRITE;

			if (mappedStatement.getSqlCommandType() == SqlCommandType.SELECT) {
				key = ConnectionHold.READ;
			} 
			routeConnection(key, conn);
		}

		return invocation.proceed();

	}
	
	private void routeConnection(String key, Connection conn) {
		ConnectionHold.currentDataSource.set(key);
		
		if(ConnectionHold.ConnectionContext.get() == null){
			ConnectionHold.ConnectionContext.set(new HashMap<String, Connection>());
		}
		if (!ConnectionHold.ConnectionContext.get().containsKey(key)) {
			ConnectionProxy conToUse = (ConnectionProxy) conn;
			conToUse.getTargetConnection();
		}
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
    /**
     * MyBatis wraps the JDBC Connection with a logging proxy but Spring registers the original connection so it should
     * be unwrapped before calling {@code DataSourceUtils.isConnectionTransactional(Connection, DataSource)}
     * 
     * @param connection May be a {@code ConnectionLogger} proxy
     * @return the original JDBC {@code Connection}
     */
    private Connection unwrapConnection(Connection connection) {
        if (Proxy.isProxyClass(connection.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(connection);
            if (handler instanceof ConnectionLogger) {
                return ((ConnectionLogger) handler).getConnection();
            }
        }
        return connection;
    }

}
