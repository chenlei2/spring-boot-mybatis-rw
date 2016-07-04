package org.spring.boot.mybatis.rw.starter.transaction;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;

/**
 * 
 * @author chenlei
 *
 */

public class RWManagedTransactionFactory extends SpringManagedTransactionFactory {

	/**
	 * {@inheritDoc}
	 */
	public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
		return new RWManagedTransaction(dataSource);
	}

}
