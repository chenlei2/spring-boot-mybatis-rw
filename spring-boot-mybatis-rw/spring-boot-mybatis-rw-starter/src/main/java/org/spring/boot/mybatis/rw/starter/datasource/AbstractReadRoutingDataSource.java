package org.spring.boot.mybatis.rw.starter.datasource;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
/**
 * 
 * @author chenlei
 *
 */
public abstract class AbstractReadRoutingDataSource implements InitializingBean {

	// 配置文件中配置的read-only datasoure
	// 可以为真实的datasource，也可以jndi的那种
	private List<Object> readDataSoures;
	private Object writeDataSource;

	private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

	private List<DataSource> resolvedReadDataSources;
	
	private DataSource resolvedWriteDataSource;
	// read-only data source的数量,做负载均衡的时候需要
	private int readDsSize;


	public List<DataSource> getResolvedReadDataSources() {
		return resolvedReadDataSources;
	}

	public int getReadDsSize() {
		return readDsSize;
	}

	public void setReadDataSoures(List<Object> readDataSoures) {
		this.readDataSoures = readDataSoures;
	}

	public void setWriteDataSource(Object writeDataSource) {
		this.writeDataSource = writeDataSource;
	}
	/**
	 * Set the DataSourceLookup implementation to use for resolving data source
	 * name Strings in the {@link #setTargetDataSources targetDataSources} map.
	 * <p>Default is a {@link JndiDataSourceLookup}, allowing the JNDI names
	 * of application server DataSources to be specified directly.
	 */
	public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = (dataSourceLookup != null ? dataSourceLookup : new JndiDataSourceLookup());
	}


	@Override
	public void afterPropertiesSet() {
			
		if (writeDataSource == null) {
			throw new IllegalArgumentException("Property 'writeDataSource' is required");
		}
		this.resolvedWriteDataSource = resolveSpecifiedDataSource(writeDataSource);

		if (this.readDataSoures == null || this.readDataSoures.size() ==0) {
			throw new IllegalArgumentException("Property 'resolvedReadDataSources' is required");
		}
		resolvedReadDataSources = new ArrayList<DataSource>(readDataSoures.size());
		for (Object item : readDataSoures) {
			resolvedReadDataSources.add(resolveSpecifiedDataSource(item));
		}
		readDsSize = readDataSoures.size();
	}

	/**
	 * Resolve the specified data source object into a DataSource instance.
	 * <p>The default implementation handles DataSource instances and data source
	 * names (to be resolved via a {@link #setDataSourceLookup DataSourceLookup}).
	 * @param dataSource the data source value object as specified in the
	 * {@link #setTargetDataSources targetDataSources} map
	 * @return the resolved DataSource (never {@code null})
	 * @throws IllegalArgumentException in case of an unsupported value type
	 */
	protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
		if (dataSource instanceof DataSource) {
			return (DataSource) dataSource;
		}
		else if (dataSource instanceof String) {
			return this.dataSourceLookup.getDataSource((String) dataSource);
		}
		else {
			throw new IllegalArgumentException(
					"Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
		}
	}

	public DataSource getTargetDataSource() {
		if (ConnectionHold.WRITE.equals(ConnectionHold.currentDataSource.get())) {
			return resolvedWriteDataSource;
		} else {
			return loadBalance();
		}
	}

	protected abstract DataSource loadBalance();
}
