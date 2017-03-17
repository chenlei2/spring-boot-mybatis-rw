package org.spring.boot.mybatis.rw.starter.datasource;

import javax.sql.DataSource;
/**
 * 数据库路由
 * @author chelei
 *
 */
public interface DataSourceRout {
	/**
	 * 根据自己的需要，实现数据库路由，可以是读写分离的数据源，或者是分表后的数据源
	 * @return
	 */
	public DataSource getTargetDataSource();

}
