package org.spring.boot.mybatis.rw.starter.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.mybatis.spring.transaction.SpringManagedTransaction;
import org.spring.boot.mybatis.rw.starter.datasource.ConnectionHold;

/**
 * 
 * @author chenlei
 *同一个线程下最多只有一个读数据源和一个写数据源
 *读数据源事务比较弱，只要保证写数据源提交/回滚/关闭正常就表示整个数据事务正常，不再关心读数据源是否正常，所以先处理写数据源，
 *一旦写数据源成功，表示整个事务成功。
 */
public class RWManagedTransaction extends SpringManagedTransaction {

	public RWManagedTransaction(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * {@inheritDoc}
	 */
	public void commit() throws SQLException {
		Map<String, Connection> connectionMap = ConnectionHold.CONNECTION_CONTEXT.get();
		Connection writeCon = connectionMap.remove(ConnectionHold.WRITE);
		if(writeCon != null){
			writeCon.commit();
		}
		Connection readCon = connectionMap.remove(ConnectionHold.READ);
		if(readCon != null){
			try {
				readCon.commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void rollback() throws SQLException {
		Map<String, Connection> connectionMap = ConnectionHold.CONNECTION_CONTEXT.get();
		Connection writeCon = connectionMap.remove(ConnectionHold.WRITE);
		if(writeCon != null){
			writeCon.rollback();
		}
		Connection readCon = connectionMap.remove(ConnectionHold.READ);
		if(readCon != null){
			try {
				readCon.rollback();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}

	/**
	 * {@inheritDoc}
	 */
	public void close() throws SQLException {
		Map<String, Connection> connectionMap = ConnectionHold.CONNECTION_CONTEXT.get();
		Connection writeCon = connectionMap.remove(ConnectionHold.WRITE);
		if(writeCon != null){
			writeCon.close();
		}
		Connection readCon = connectionMap.remove(ConnectionHold.READ);
		if(readCon != null){
			try {
				readCon.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		ConnectionHold.CURRENT_CONNECTION.set(ConnectionHold.WRITE);
		ConnectionHold.FORCE_WRITE.set(false);
	}
}
