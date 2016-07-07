package org.spring.boot.mybatis.rw.starter.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NamedThreadLocal;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.util.Assert;

/**
 * @author chenlei 
 *
 *借鉴 LazyConnectionDataSourceProxy，AbstractRoutingDataSource
 */
public abstract class AbstractRWRoutingDataSourceProxy extends AbstractDataSource implements InitializingBean {

	public static final String READ = "read";
	public static final String WRITE = "write";

	public final static ThreadLocal<String> currentDataSource = new NamedThreadLocal<String>("routingdatasource's key");
	public final static ThreadLocal<Map<String, Connection>> ConnectionContext = new NamedThreadLocal<Map<String, Connection>>(
			"connection map");
	
	public final static ThreadLocal<Boolean>  FORCE_WRITE = new NamedThreadLocal<Boolean>("FORCE_WRITE");
	

	// 配置文件中配置的read-only datasoure
	// 可以为真实的datasource，也可以jndi的那种
	private List<Object> readDataSoures;
	private List<DataSource> resolvedReadDataSources;

	private Object writeDataSource;
	private DataSource resolvedWriteDataSource;
	// read-only data source的数量,做负载均衡的时候需要
	private int readDsSize;

	private boolean defaultAutoCommit = true;
	private int defaultTransactionIsolation = Connection.TRANSACTION_READ_COMMITTED;

	private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

	public Connection getConnection() throws SQLException {

		return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(),
				new Class[] { ConnectionProxy.class }, new RWConnectionInvocationHandler());
	}

	public Connection getConnection(String username, String password) throws SQLException {

		return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(),
				new Class[] { ConnectionProxy.class }, new RWConnectionInvocationHandler(username, password));
	}

	public int getReadDsSize() {
		return readDsSize;
	}

	public List<DataSource> getResolvedReadDataSources() {
		return resolvedReadDataSources;
	}

	public DataSource getResolvedWriteDataSource() {
		return resolvedWriteDataSource;
	}

	public void afterPropertiesSet() throws Exception {	
		if (writeDataSource == null) {
			throw new IllegalArgumentException("Property 'writeDataSource' is required");
		}
		this.resolvedWriteDataSource = resolveSpecifiedDataSource(writeDataSource);

		resolvedReadDataSources = new ArrayList<DataSource>(readDataSoures.size());
		for (Object item : readDataSoures) {
			resolvedReadDataSources.add(resolveSpecifiedDataSource(item));
		}
		readDsSize = readDataSoures.size();

	}

	public void setReadDataSoures(List<Object> readDataSoures) {
		this.readDataSoures = readDataSoures;
	}

	public void setWriteDataSource(Object writeDataSource) {
		this.writeDataSource = writeDataSource;
	}

	protected DataSource determineTargetDataSource() {
		Assert.notNull(this.resolvedReadDataSources, "DataSource router not initialized");
		// String lookupKey = determineCurrentLookupKey();
		if (WRITE.equals(currentDataSource.get())) {
			return resolvedWriteDataSource;
		} else {
			return loadBalance();
		}
	}

	/**
	 * 获取真实的data source
	 * 
	 * @param dataSource
	 *            (jndi | real data source)
	 * @return
	 * @throws IllegalArgumentException
	 */
	protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
		if (dataSource instanceof DataSource) {
			return (DataSource) dataSource;
		} else if (dataSource instanceof String) {
			return this.dataSourceLookup.getDataSource((String) dataSource);
		} else {
			throw new IllegalArgumentException(
					"Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
		}
	}

	protected abstract DataSource loadBalance();

	/**
	 * Invocation handler that defers fetching an actual JDBC Connection until
	 * first creation of a Statement.
	 */
	private class RWConnectionInvocationHandler implements InvocationHandler {

		private String username;

		private String password;

		private Boolean readOnly = Boolean.FALSE;

		private Integer transactionIsolation;

		private Boolean autoCommit;

		private boolean closed = false;

		public RWConnectionInvocationHandler() {

		}

		public RWConnectionInvocationHandler(String username, String password) {
			this();
			this.username = username;
			this.password = password;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on ConnectionProxy interface coming in...

			if (method.getName().equals("equals")) {
				// We must avoid fetching a target Connection for "equals".
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			} else if (method.getName().equals("hashCode")) {
				// We must avoid fetching a target Connection for "hashCode",
				// and we must return the same hash code even when the target
				// Connection has been fetched: use hashCode of Connection
				// proxy.
				return new Integer(System.identityHashCode(proxy));
			} else if (method.getName().equals("getTargetConnection")) {
				// Handle getTargetConnection method: return underlying
				// connection.
				return getTargetConnection(method);
			}

			if (!hasTargetConnection()) {
				// No physical target Connection kept yet ->
				// resolve transaction demarcation methods without fetching
				// a physical JDBC Connection until absolutely necessary.

				if (method.getName().equals("toString")) {
					return "RW Routing DataSource Proxy";
				} else if (method.getName().equals("isReadOnly")) {
					return this.readOnly;
				} else if (method.getName().equals("setReadOnly")) {
					this.readOnly = (Boolean) args[0];
					return null;
				} else if (method.getName().equals("getTransactionIsolation")) {
					if (this.transactionIsolation != null) {
						return this.transactionIsolation;
					}
					return defaultTransactionIsolation;
					// Else fetch actual Connection and check there,
					// because we didn't have a default specified.
				} else if (method.getName().equals("setTransactionIsolation")) {
					this.transactionIsolation = (Integer) args[0];
					return null;
				} else if (method.getName().equals("getAutoCommit")) {
					if (this.autoCommit != null)
						return this.autoCommit;
					return defaultAutoCommit;
					// Else fetch actual Connection and check there,
					// because we didn't have a default specified.
				} else if (method.getName().equals("setAutoCommit")) {
					this.autoCommit = (Boolean) args[0];
					return null;
				} else if (method.getName().equals("commit")) {
					// Ignore: no statements created yet.
					return null;
				} else if (method.getName().equals("rollback")) {
					// Ignore: no statements created yet.
					return null;
				} else if (method.getName().equals("getWarnings")) {
					return null;
				} else if (method.getName().equals("clearWarnings")) {
					return null;
				} else if (method.getName().equals("isClosed")) {
					return (this.closed ? Boolean.TRUE : Boolean.FALSE);
				} else if (method.getName().equals("close")) {
					// Ignore: no target connection yet.
					this.closed = true;
					return null;
				} else if (this.closed) {
					// Connection proxy closed, without ever having fetched a
					// physical JDBC Connection: throw corresponding
					// SQLException.
					throw new SQLException("Illegal operation: connection is closed");
				}
			}

			// Target Connection already fetched,
			// or target Connection necessary for current operation ->
			// invoke method on target connection.
			try {
				if (!hasTargetConnection()) {
					getTargetConnection(method);
				} 
				return method.invoke(ConnectionContext.get().get(currentDataSource.get()), args);
			} catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		/**
		 * Return whether the proxy currently holds a target Connection.
		 */
		private boolean hasTargetConnection() {
			return (ConnectionContext.get() !=null && ConnectionContext.get().get(currentDataSource.get()) != null);
		}

		/**
		 * Return the target Connection, fetching it and initializing it if
		 * necessary.
		 */
		private Connection getTargetConnection(Method operation) throws SQLException {

			// No target Connection held -> fetch one.
			if (logger.isDebugEnabled()) {
				logger.debug("Connecting to database for operation '" + operation.getName() + "'");
			}

			// Fetch physical Connection from DataSource.
			Connection target = (this.username != null)
					? determineTargetDataSource().getConnection(this.username, this.password)
					: determineTargetDataSource().getConnection();

			// If we still lack default connection properties, check them now.
			// checkDefaultConnectionProperties(this.target);

			// Apply kept transaction settings, if any.
			if (this.readOnly.booleanValue()) {
				target.setReadOnly(this.readOnly.booleanValue());
			}
			if (this.transactionIsolation != null) {
				target.setTransactionIsolation(this.transactionIsolation.intValue());
			}
			if (this.autoCommit != null && this.autoCommit.booleanValue() != target.getAutoCommit()) {
				target.setAutoCommit(this.autoCommit.booleanValue());
			}
			
			if(ConnectionContext.get() == null){
				ConnectionContext.set(new HashMap<String, Connection>());
			}
			ConnectionContext.get().put(currentDataSource.get(), target);	
			return target;
		}
	}

}
