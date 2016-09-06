package org.spring.boot.mybatis.rw.starter.datasource;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Constants;
import org.springframework.jdbc.datasource.ConnectionProxy;

/**
 * 
 * @author chenlei
 *
 */
@Deprecated
public class LazyConnectionDataSourceProxy implements DataSource {

	/** Constants instance for TransactionDefinition */
	private static final Constants constants = new Constants(Connection.class);

	private static final Log logger = LogFactory.getLog(LazyConnectionDataSourceProxy.class);

	private Boolean defaultAutoCommit;

	private Integer defaultTransactionIsolation;
	
	private AbstractReadRoutingDataSource abstractReadRoutingDataSource;

	/**
	 * Create a new LazyConnectionDataSourceProxy.
	 * 
	 * @see #setTargetDataSource
	 */
	public LazyConnectionDataSourceProxy(AbstractReadRoutingDataSource abstractReadRoutingDataSource) {
		this.abstractReadRoutingDataSource = abstractReadRoutingDataSource;
	}

	/**
	 * Set the default auto-commit mode to expose when no target Connection has
	 * been fetched yet (-> actual JDBC Connection default not known yet).
	 * <p>
	 * If not specified, the default gets determined by checking a target
	 * Connection on startup. If that check fails, the default will be
	 * determined lazily on first access of a Connection.
	 * 
	 * @see java.sql.Connection#setAutoCommit
	 */
	public void setDefaultAutoCommit(boolean defaultAutoCommit) {
		this.defaultAutoCommit = defaultAutoCommit;
	}

	/**
	 * Set the default transaction isolation level to expose when no target
	 * Connection has been fetched yet (-> actual JDBC Connection default not
	 * known yet).
	 * <p>
	 * This property accepts the int constant value (e.g. 8) as defined in the
	 * {@link java.sql.Connection} interface; it is mainly intended for
	 * programmatic use. Consider using the "defaultTransactionIsolationName"
	 * property for setting the value by name (e.g. "TRANSACTION_SERIALIZABLE").
	 * <p>
	 * If not specified, the default gets determined by checking a target
	 * Connection on startup. If that check fails, the default will be
	 * determined lazily on first access of a Connection.
	 * 
	 * @see #setDefaultTransactionIsolationName
	 * @see java.sql.Connection#setTransactionIsolation
	 */
	public void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
		this.defaultTransactionIsolation = defaultTransactionIsolation;
	}

	/**
	 * Set the default transaction isolation level by the name of the
	 * corresponding constant in {@link java.sql.Connection}, e.g.
	 * "TRANSACTION_SERIALIZABLE".
	 * 
	 * @param constantName
	 *            name of the constant
	 * @see #setDefaultTransactionIsolation
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	public void setDefaultTransactionIsolationName(String constantName) {
		setDefaultTransactionIsolation(constants.asNumber(constantName).intValue());
	}

	/**
	 * Expose the default auto-commit value.
	 */
	protected Boolean defaultAutoCommit() {
		return this.defaultAutoCommit;
	}

	/**
	 * Expose the default transaction isolation value.
	 */
	protected Integer defaultTransactionIsolation() {
		return this.defaultTransactionIsolation;
	}

	/**
	 * Return a Connection handle that lazily fetches an actual JDBC Connection
	 * when asked for a Statement (or PreparedStatement or CallableStatement).
	 * <p>
	 * The returned Connection handle implements the ConnectionProxy interface,
	 * allowing to retrieve the underlying target Connection.
	 * 
	 * @return a lazy Connection handle
	 * @see ConnectionProxy#getTargetConnection()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(),
				new Class<?>[] { ConnectionProxy.class }, new LazyConnectionInvocationHandler());
	}

	/**
	 * Return a Connection handle that lazily fetches an actual JDBC Connection
	 * when asked for a Statement (or PreparedStatement or CallableStatement).
	 * <p>
	 * The returned Connection handle implements the ConnectionProxy interface,
	 * allowing to retrieve the underlying target Connection.
	 * 
	 * @param username
	 *            the per-Connection username
	 * @param password
	 *            the per-Connection password
	 * @return a lazy Connection handle
	 * @see ConnectionProxy#getTargetConnection()
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(),
				new Class<?>[] { ConnectionProxy.class }, new LazyConnectionInvocationHandler(username, password));
	}

	/**
	 * Invocation handler that defers fetching an actual JDBC Connection until
	 * first creation of a Statement.
	 */
	private class LazyConnectionInvocationHandler implements InvocationHandler {

		private String username;

		private String password;

		private Boolean readOnly = Boolean.FALSE;

		private Integer transactionIsolation;

		private Boolean autoCommit;

		private boolean closed = false;

		public LazyConnectionInvocationHandler() {
			this.autoCommit = defaultAutoCommit();
			this.transactionIsolation = defaultTransactionIsolation();
		}

		public LazyConnectionInvocationHandler(String username, String password) {
			this();
			this.username = username;
			this.password = password;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on ConnectionProxy interface coming in...

			if (method.getName().equals("equals")) {
				// We must avoid fetching a target Connection for "equals".
				// Only consider equal when proxies are identical.
				return (proxy == args[0]);
			} else if (method.getName().equals("hashCode")) {
				// We must avoid fetching a target Connection for "hashCode",
				// and we must return the same hash code even when the target
				// Connection has been fetched: use hashCode of Connection
				// proxy.
				return System.identityHashCode(proxy);
			} else if (method.getName().equals("unwrap")) {
				if (((Class<?>) args[0]).isInstance(proxy)) {
					return proxy;
				}
			} else if (method.getName().equals("isWrapperFor")) {
				if (((Class<?>) args[0]).isInstance(proxy)) {
					return true;
				}
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
					return "Lazy Connection proxy for target DataSource [" + abstractReadRoutingDataSource.getTargetDataSource() + "]";
				} else if (method.getName().equals("isReadOnly")) {
					return this.readOnly;
				} else if (method.getName().equals("setReadOnly")) {
					this.readOnly = (Boolean) args[0];
					return null;
				} else if (method.getName().equals("getTransactionIsolation")) {
					if (this.transactionIsolation != null) {
						return this.transactionIsolation;
					}
					// Else fetch actual Connection and check there,
					// because we didn't have a default specified.
				} else if (method.getName().equals("setTransactionIsolation")) {
					this.transactionIsolation = (Integer) args[0];
					return null;
				} else if (method.getName().equals("getAutoCommit")) {
					if (this.autoCommit != null) {
						return this.autoCommit;
					}
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
				} else if (method.getName().equals("close")) {
					// Ignore: no target connection yet.
					this.closed = true;
					return null;
				} else if (method.getName().equals("isClosed")) {
					return this.closed;
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
				return method.invoke(ConnectionHold.ConnectionContext.get().get(ConnectionHold.currentDataSource.get()), args);
			} catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		/**
		 * Return whether the proxy currently holds a target Connection.
		 */
		private boolean hasTargetConnection() {
			return (ConnectionHold.ConnectionContext.get() != null 
					&& ConnectionHold.ConnectionContext.get().get(ConnectionHold.currentDataSource.get()) != null);
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
					? abstractReadRoutingDataSource.getTargetDataSource().getConnection(this.username, this.password)
					: abstractReadRoutingDataSource.getTargetDataSource().getConnection();

			// Apply kept transaction settings, if any.
			if (this.readOnly) {
				try {
					target.setReadOnly(this.readOnly);
				} catch (Exception ex) {
					// "read-only not supported" -> ignore, it's just a hint
					// anyway
					logger.debug("Could not set JDBC Connection read-only", ex);
				}
			}
			if (this.transactionIsolation != null && !this.transactionIsolation.equals(defaultTransactionIsolation())) {
				target.setTransactionIsolation(this.transactionIsolation);
			}
			if (this.autoCommit != null && this.autoCommit != target.getAutoCommit()) {
				target.setAutoCommit(this.autoCommit);
			}

			if (ConnectionHold.ConnectionContext.get() == null) {
				ConnectionHold.ConnectionContext.set(new HashMap<String, Connection>());
			}
			if(ConnectionHold.currentDataSource.get() == null){
				ConnectionHold.currentDataSource.set(ConnectionHold.WRITE);
			}
			ConnectionHold.ConnectionContext.get().put(ConnectionHold.currentDataSource.get(), target);
			return target;
		}
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return abstractReadRoutingDataSource.getTargetDataSource().getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		abstractReadRoutingDataSource.getTargetDataSource().setLogWriter(out);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return abstractReadRoutingDataSource.getTargetDataSource().getLoginTimeout();
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		abstractReadRoutingDataSource.getTargetDataSource().setLoginTimeout(seconds);
	}

	// ---------------------------------------------------------------------
	// Implementation of JDBC 4.0's Wrapper interface
	// ---------------------------------------------------------------------

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return (T) this;
		}
		return abstractReadRoutingDataSource.getTargetDataSource().unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return (iface.isInstance(this) || abstractReadRoutingDataSource.getTargetDataSource().isWrapperFor(iface));
	}

	// ---------------------------------------------------------------------
	// Implementation of JDBC 4.1's getParentLogger method
	// ---------------------------------------------------------------------

	@Override
	public Logger getParentLogger() {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}
}
