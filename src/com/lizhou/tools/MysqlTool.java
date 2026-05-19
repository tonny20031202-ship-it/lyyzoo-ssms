package com.lizhou.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class MysqlTool {
	private static ComboPooledDataSource dataSource = null;
	
	private static ThreadLocal<ConnectionHolder> tl = new ThreadLocal<ConnectionHolder>();
	
	static {
		dataSource = new ComboPooledDataSource();
	}
	
	private static class ConnectionHolder {
		Connection connection;
		int refCount;
		boolean originalAutoCommit;
		
		ConnectionHolder(Connection conn) throws SQLException {
			this.connection = conn;
			this.originalAutoCommit = conn.getAutoCommit();
			this.refCount = 1;
		}
	}
	
	public static DataSource getDataSource(){
		return dataSource;
	}
	
	public static Connection getConnection(){
		return getConnection(true);
	}
	
	public static Connection getConnection(boolean autoCommit){
		ConnectionHolder holder = tl.get();
		try {
			if(holder == null){
				Connection conn = dataSource.getConnection();
				holder = new ConnectionHolder(conn);
				tl.set(holder);
			} else {
				holder.refCount++;
			}
			holder.connection.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return holder != null ? holder.connection : null;
	}
	
	/**
	 * 开始事务
	 * @throws SQLException
	 */
	public static void startTransaction(){
		Connection conn = getConnection();
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 回滚事务
	 * @throws SQLException
	 */
	public static void rollback(){
		ConnectionHolder holder = tl.get();
		if(holder != null && holder.connection != null){
			try {
				if(!holder.connection.getAutoCommit()){
					holder.connection.rollback();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 提交事务
	 * @throws SQLException
	 */
	public static void commit(){
		ConnectionHolder holder = tl.get();
		if(holder != null && holder.connection != null){
			try {
				if(!holder.connection.getAutoCommit()){
					holder.connection.commit();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 关闭Connection,并移除线程中的连接
	 * @throws SQLException
	 */
	public static void closeConnection(){
		ConnectionHolder holder = tl.get();
		if(holder != null){
			holder.refCount--;
			if(holder.refCount <= 0){
				try {
					if(holder.connection != null && !holder.connection.isClosed()){
						holder.connection.setAutoCommit(holder.originalAutoCommit);
						holder.connection.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					tl.remove();
				}
			}
		}
	}
	
	public static void close(Connection conn){
		try {
			if(conn != null && !conn.isClosed()){
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(Statement stm){
		try {
			if(stm != null){
				stm.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(ResultSet rs){
		try {
			if(rs != null){
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
