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
		AbstractRWRoutingDataSourceProxy.currentDataSource.remove();
		AbstractRWRoutingDataSourceProxy.currentDataSource.set(AbstractRWRoutingDataSourceProxy.WRITE);
		if (this.dataSource != null) {
			ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.dataSource);
			if (conHolder != null && connectionEquals(conHolder, super.getConnection())) {
				// It's the transactional Connection: Don't close it.
				conHolder.released();
			}
		}

	}
	/**
	 * Determine whether the given two Connections are equal, asking the target
	 * Connection in case of a proxy. Used to detect equality even if the
	 * user passed in a raw target Connection while the held one is a proxy.
	 * @param conHolder the ConnectionHolder for the held Connection (potentially a proxy)
	 * @param passedInCon the Connection passed-in by the user
	 * (potentially a target Connection without proxy)
	 * @return whether the given Connections are equal
	 * @see #getTargetConnection
	 */
	private static boolean connectionEquals(ConnectionHolder conHolder, Connection passedInCon) {
		
		if (conHolder.getConnectionHandle() == null) {
			return false;
		}
		Connection heldCon = conHolder.getConnection();
		// Explicitly check for identity too: for Connection handles that do not implement
		// "equals" properly, such as the ones Commons DBCP exposes).
		return (heldCon == passedInCon || heldCon.equals(passedInCon) ||
				DataSourceUtils.getTargetConnection(heldCon).equals(passedInCon));
	}
}
