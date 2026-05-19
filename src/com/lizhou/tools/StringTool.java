package com.lizhou.tools;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 字符串工具类
 * @author bojiangzhou
 *
 */
public class StringTool {

	private static final int MAX_CACHE_SIZE = 200;

	private static final Map<String, Boolean> isEmptyCache = Collections.synchronizedMap(
			new LinkedHashMap<String, Boolean>(MAX_CACHE_SIZE, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
					return size() > MAX_CACHE_SIZE;
				}
			});

	private static final Map<Integer, String> getMarkCache = Collections.synchronizedMap(
			new LinkedHashMap<Integer, String>(MAX_CACHE_SIZE, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
					return size() > MAX_CACHE_SIZE;
				}
			});

	/**
	 * 判断字符串是否为空
	 * @param str
	 * @return true: 空 | false: 不为空
	 */
	public static boolean isEmpty(String str){
		String key = normalize(str);
		synchronized (isEmptyCache) {
			if (isEmptyCache.containsKey(key)) {
				return isEmptyCache.get(key);
			}
		}
		boolean result = (str == null || "".equals(str.trim()));
		synchronized (isEmptyCache) {
			isEmptyCache.put(key, result);
		}
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
		synchronized (getMarkCache) {
			if (getMarkCache.containsKey(length)) {
				return getMarkCache.get(length);
			}
		}
		StringBuffer mark = new StringBuffer("");
		for(int i = 0;i < length;i++){
			mark.append("?,");
		}
		mark.deleteCharAt(mark.length()-1);
		String result = mark.toString();
		synchronized (getMarkCache) {
			getMarkCache.put(length, result);
		}
		return result;
	}

	/**
	 * 清空所有缓存
	 */
	public static void clearCache(){
		synchronized (isEmptyCache) {
			isEmptyCache.clear();
		}
		synchronized (getMarkCache) {
			getMarkCache.clear();
		}
	}

	/**
	 * 规范化字符串作为缓存key
	 * @param str
	 * @return 规范化后的key
	 */
	public static String normalize(String str){
		if (str == null) {
			return "__NULL__";
		}
		return str.trim();
	}

}
