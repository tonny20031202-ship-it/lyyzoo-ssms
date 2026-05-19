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
	
	static {
		dataSource = new ComboPooledDataSource();
	}
	
	public static DataSource getDataSource(){
		return dataSource;
	}
	
	public static Connection getConnection(){
		Connection conn = getThreadConnection();
		if(conn != null){
			return conn;
		}
		return getConnection(true);
	}
	
	public static Connection getConnection(boolean autoCommit){
		Connection conn = getThreadConnection();
		try {
			if(conn == null){
				conn = dataSource.getConnection();
				tl.set(conn);
			}
			if(conn.getAutoCommit() != autoCommit){
				conn.setAutoCommit(autoCommit);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	private static Connection getThreadConnection(){
		Connection conn = tl.get();
		if(conn == null){
			return null;
		}
		try {
			if(conn.isClosed()){
				tl.remove();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			tl.remove();
			return null;
		}
		return conn;
	}
	
	/**
	 * 开始事务
	 * @throws SQLException
	 */
	public static void startTransaction(){
		getConnection(false);
	}
	
	/**
	 * 回滚事务
	 * @throws SQLException
	 */
	public static void rollback(){
		Connection conn = getThreadConnection();
		if(conn == null){
			return;
		}
		try {
			if(!conn.getAutoCommit()){
				conn.rollback();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 提交事务
	 * @throws SQLException
	 */
	public static void commit(){
		Connection conn = getThreadConnection();
		if(conn == null){
			return;
		}
		try {
			if(!conn.getAutoCommit()){
				conn.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭Connection,并移除线程中的连接
	 * @throws SQLException
	 */
	public static void closeConnection(){
		Connection conn = tl.get();
		tl.remove();
		close(conn);
	}
	
	public static void close(Connection conn){
		try {
			if(conn != null && !conn.isClosed()){
				if(!conn.getAutoCommit()){
					conn.setAutoCommit(true);
				}
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
