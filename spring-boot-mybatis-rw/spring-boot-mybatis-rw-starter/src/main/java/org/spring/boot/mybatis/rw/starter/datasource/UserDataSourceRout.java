package org.spring.boot.mybatis.rw.starter.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
/**
 * 数据库路由
 * @author chelei
 *
 */
public class UserDataSourceRout implements DataSourceRout{
	/**
	 * 应用启动时配置一组读写分离数据组，根据不同的用户路由到不同的数据组上。后期不在修改，属于不变模式，不考虑线程安全问题
	 */
	private Map<String, AbstractRWDataSourceRout> userDataSource = new HashMap<String, AbstractRWDataSourceRout>();
	
	public UserDataSourceRout(Map<String, AbstractRWDataSourceRout> userDataSource) {
		this.userDataSource = userDataSource;
	}

	@Override
	public DataSource getTargetDataSource() {
		AbstractRWDataSourceRout currentDataSource = userDataSource.get(DataSourceHold.CURRENT_DATASOURCE.get());
		return currentDataSource.getTargetDataSource();
	}

}
