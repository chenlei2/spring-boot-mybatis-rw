package org.spring.boot.mybatis.rw.starter.pulgin;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.spring.boot.mybatis.rw.starter.datasource.ConnectionHold;
/**
 * 不侵入mybatis的逻辑，实现读写分离
 * 
 * @author chenlei
 *
 */
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
	@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }) })
public class RWPlugin implements Interceptor {

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		
		if(ConnectionHold.FORCE_WRITE.get() != null && ConnectionHold.FORCE_WRITE.get()){
			ConnectionHold.currentDataSource.set(ConnectionHold.WRITE);
			return invocation.proceed();	
		}	
		MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
		if (mappedStatement.getSqlCommandType() == SqlCommandType.SELECT) {
			ConnectionHold.currentDataSource.set(ConnectionHold.READ);
		} else {
			ConnectionHold.currentDataSource.set(ConnectionHold.WRITE);
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		if (target instanceof Executor) {  
            return Plugin.wrap(target, this);  
        } else {  
            return target;  
        }  
	}

	@Override
	public void setProperties(Properties properties) {
		// NOOP
	}
}
