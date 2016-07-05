package org.spring.boot.mybatis.rw.starter.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.mybatis.spring.transaction.SpringManagedTransaction;
import org.spring.boot.mybatis.rw.starter.datasource.AbstractRWRoutingDataSourceProxy;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 
 * @author chenlei
 *
 */
public class RWManagedTransaction extends SpringManagedTransaction {

	private final DataSource dataSource;
	public RWManagedTransaction(DataSource dataSource) {
		super(dataSource);
		this.dataSource  = dataSource;
	}

	/**
	 * {@inheritDoc}
	 */
	public void commit() throws SQLException {
		super.commit();
		Map<String, Connection> connectionMap = AbstractRWRoutingDataSourceProxy.ConnectionContext.get();
		if(connectionMap !=null){
			for (Connection c : connectionMap.values()) {
				if(!c.isClosed() && !c.getAutoCommit()){
					c.commit();
				}
			}
		}	
	}

	/**
	 * {@inheritDoc}
	 */
	public void rollback() throws SQLException {
		super.rollback();
		Map<String, Connection> connectionMap = AbstractRWRoutingDataSourceProxy.ConnectionContext.get();
		if(connectionMap !=null){
			for (Connection c : connectionMap.values()) {
				if(!c.isClosed() && !c.getAutoCommit()){
					c.rollback();
				}
			}
		}		
	}

	/**
	 * {@inheritDoc}
	 */
	public void close() throws SQLException {
		super.close();
		Map<String, Connection> connectionMap = AbstractRWRoutingDataSourceProxy.ConnectionContext.get();
		if(connectionMap !=null){
			for (Connection c : connectionMap.values()) {
				c.close();
			}
		}	
		AbstractRWRoutingDataSourceProxy.ConnectionContext.remove();;
		AbstractRWRoutingDataSourceProxy.currentDataSource.set(AbstractRWRoutingDataSourceProxy.WRITE);
	}
}
