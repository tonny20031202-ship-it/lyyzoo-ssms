package com.lizhou.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class MysqlTool {
	private static ComboPooledDataSource dataSource = null;
	
	private static ThreadLocal<Connection> tl = new ThreadLocal<Connection>();
	
	private static ThreadLocal<Integer> transactionLevel = new ThreadLocal<Integer>();
	
	static {
		dataSource = new ComboPooledDataSource();
	}
	
	public static DataSource getDataSource(){
		return dataSource;
	}
	
	public static Connection getConnection(){
		return getConnection(true);
	}
	
	public static Connection getConnection(boolean autoCommit){
		Connection conn = tl.get();
		try {
			if(conn == null){
				conn = dataSource.getConnection();
				conn.setAutoCommit(autoCommit);
				tl.set(conn);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * 开始事务
	 * @throws SQLException
	 */
	public static void startTransaction(){
		Connection conn = getConnection(false);
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Integer level = transactionLevel.get();
		if(level == null){
			level = 0;
		}
		transactionLevel.set(level + 1);
	}
	
	/**
	 * 回滚事务
	 * @throws SQLException
	 */
	public static void rollback(){
		Integer level = transactionLevel.get();
		if(level == null || level == 0){
			return;
		}
		Connection conn = tl.get();
		if(conn != null){
			try {
				conn.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		transactionLevel.set(0);
	}
	
	/**
	 * 提交事务
	 * @throws SQLException
	 */
	public static void commit(){
		Integer level = transactionLevel.get();
		if(level == null || level == 0){
			return;
		}
		level = level - 1;
		if(level == 0){
			Connection conn = tl.get();
			if(conn != null){
				try {
					conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		transactionLevel.set(level);
	}
	
	/**
	 * 关闭Connection,并移除线程中的连接
	 * @throws SQLException
	 */
	public static void closeConnection(){
		Connection conn = tl.get();
		if(conn != null){
			try {
				if(!conn.getAutoCommit()){
					conn.setAutoCommit(true);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			close(conn);
		}
		tl.remove();
		transactionLevel.remove();
	}
	
	public static void close(Connection conn){
		try {
			if(conn != null){
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
