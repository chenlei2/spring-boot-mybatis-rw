package org.spring.boot.mybatis.rw.starter.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.springframework.jdbc.datasource.AbstractDataSource;
/**
 * 
 * @author chenlei
 *
 */
public class DataSourceProxy extends AbstractDataSource {
	
	private AbstractReadRoutingDataSource abstractReadRoutingDataSource;
	

	public DataSourceProxy(AbstractReadRoutingDataSource abstractReadRoutingDataSource) {
		this.abstractReadRoutingDataSource = abstractReadRoutingDataSource;
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = getRWConnection();
		if(connection != null){
			return connection;
		}
		connection = abstractReadRoutingDataSource.getTargetDataSource().getConnection();
		ConnectionHold.ConnectionContext.get().put(ConnectionHold.currentDataSource.get(), connection);
		return connection;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		Connection connection = getRWConnection();
		if(connection != null){
			return connection;
		}
		connection = abstractReadRoutingDataSource.getTargetDataSource().getConnection(username, password);
		ConnectionHold.ConnectionContext.get().put(ConnectionHold.currentDataSource.get(), connection);
		return connection;
	}
	
	private Connection getRWConnection(){
		if(ConnectionHold.ConnectionContext.get() == null){
			ConnectionHold.ConnectionContext.set(new HashMap<String, Connection>());
			return null;
		}
		return ConnectionHold.ConnectionContext.get().get(ConnectionHold.currentDataSource.get());
	}

}
