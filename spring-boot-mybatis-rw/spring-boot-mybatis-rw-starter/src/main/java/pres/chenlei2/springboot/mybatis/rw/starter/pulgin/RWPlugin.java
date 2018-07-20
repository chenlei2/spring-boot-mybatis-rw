package pres.chenlei2.springboot.mybatis.rw.starter.pulgin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import pres.chenlei2.springboot.mybatis.rw.starter.datasource.ConnectionHold;
import org.springframework.jdbc.datasource.ConnectionProxy;

/**
 * 数据源读写分离路由
 * 
 * @author chenlei
 *
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}) })
public class RWPlugin implements Interceptor {

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
			String sel = statementHandler.getBoundSql().getSql().trim().substring(0,3);
			if (sel.equalsIgnoreCase("sel") && !mappedStatement.getId().endsWith(".insert!selectKey")) {
				key = ConnectionHold.READ;
			} 
			routeConnection(key, conn);
		}

		return invocation.proceed();

	}
	
	private void routeConnection(String key, Connection conn) {
		ConnectionHold.CURRENT_CONNECTION.set(key);
		// 同一个线程下保证最多只有一个写数据链接和读数据链接
		if (!ConnectionHold.CONNECTION_CONTEXT.get().containsKey(key)) {
			ConnectionProxy conToUse = (ConnectionProxy) conn;
			conn = conToUse.getTargetConnection();
			ConnectionHold.CONNECTION_CONTEXT.get().put(key, conn);
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
