package com.lizhou.tools;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 字符串工具类
 * @author bojiangzhou
 *
 */
public class StringTool {
	
	private static final int MAX_CACHE_SIZE = 200;
	
	private static final LRUCache<String, Boolean> isEmptyCache = new LRUCache<>(MAX_CACHE_SIZE);
	private static final LRUCache<Integer, String> getMarkCache = new LRUCache<>(MAX_CACHE_SIZE);
	
	/**
	 * 判断字符串是否为空
	 * @param str
	 * @return true: 空 | false: 不为空
	 */
	public static boolean isEmpty(String str){
		Boolean cached = isEmptyCache.get(str);
		if (cached != null) {
			return cached;
		}
		boolean result = str == null || "".equals(str.trim());
		isEmptyCache.put(str, result);
		return result;
	}
	
	/**
	 * 将get方式传来的中文转为UTF-8格式
	 * @param str
	 * @return
	 */
	public static String messyCode(String str){
		String code = null;
		try {
			byte[] by = str.getBytes("ISO-8859-1");
			code = new String(by, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return code;
	}
	
	/**
	 * 根据长度生成占位符：即'?,'
	 * @param length
	 * @return
	 */
	public static String getMark(int length){
		String cached = getMarkCache.get(length);
		if (cached != null) {
			return cached;
		}
		StringBuffer mark = new StringBuffer("");
		for(int i = 0;i < length;i++){
			mark.append("?,");
		}
		mark.deleteCharAt(mark.length()-1);
		String result = mark.toString();
		getMarkCache.put(length, result);
		return result;
	}
	
	/**
	 * 清空所有缓存
	 */
	public static void clearCache() {
		isEmptyCache.clear();
		getMarkCache.clear();
	}
	
	/**
	 * 规范化字符串：去除首尾空格，null 转为空字符串
	 * @param str
	 * @return 规范化后的字符串
	 */
	public static String normalize(String str) {
		return str == null ? "" : str.trim();
	}
	
	private static class LRUCache<K, V> {
		private final Map<K, V> cache;
		private final ReadWriteLock lock = new ReentrantReadWriteLock();
		
		public LRUCache(int maxSize) {
			this.cache = new LinkedHashMap<K, V>(16, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
					return size() > maxSize;
				}
			};
		}
		
		public V get(K key) {
			lock.readLock().lock();
			try {
				return cache.get(key);
			} finally {
				lock.readLock().unlock();
			}
		}
		
		public void put(K key, V value) {
			lock.writeLock().lock();
			try {
				cache.put(key, value);
			} finally {
				lock.writeLock().unlock();
			}
		}
		
		public void clear() {
			lock.writeLock().lock();
			try {
				cache.clear();
			} finally {
				lock.writeLock().unlock();
			}
		}
	}
	
}
