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
	
	private static final Map<String, Boolean> EMPTY_CACHE = Collections.synchronizedMap(
		new LinkedHashMap<String, Boolean>(16, 0.75f, true) {
			private static final long serialVersionUID = 1L;
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
				return size() > MAX_CACHE_SIZE;
			}
		});
		
	private static final Map<Integer, String> MARK_CACHE = Collections.synchronizedMap(
		new LinkedHashMap<Integer, String>(16, 0.75f, true) {
			private static final long serialVersionUID = 1L;
			@Override
			protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
				return size() > MAX_CACHE_SIZE;
			}
		});

	/**
	 * 清空缓存
	 */
	public static void clearCache() {
		EMPTY_CACHE.clear();
		MARK_CACHE.clear();
	}
	
	/**
	 * 规范化字符串
	 * @param str
	 * @return
	 */
	public static String normalize(String str) {
		return str == null ? "" : str.trim();
	}

	/**
	 * 判断字符串是否为空
	 * @param str
	 * @return true: 空 | false: 不为空
	 */
	public static boolean isEmpty(String str){
		if(str == null) {
			return true;
		}
		Boolean cached = EMPTY_CACHE.get(str);
		if (cached != null) {
			return cached;
		}
		boolean result = "".equals(str.trim());
		EMPTY_CACHE.put(str, result);
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
		String cached = MARK_CACHE.get(length);
		if (cached != null) {
			return cached;
		}
		
		StringBuffer mark = new StringBuffer("");
		for(int i = 0;i < length;i++){
			mark.append("?,");
		}
		//删除最后一个逗号
		mark.deleteCharAt(mark.length()-1);
		
		String result = mark.toString();
		MARK_CACHE.put(length, result);
		return result;
	}
	
}
