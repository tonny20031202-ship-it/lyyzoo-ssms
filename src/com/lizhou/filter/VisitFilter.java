package com.lizhou.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lizhou.bean.User;

/**
 * 如果用户没有登录，返回登录界面
 * @author bojiangzhou
 *
 */
public class VisitFilter implements Filter {

	private static final Set<String> DEFAULT_PUBLIC_PATHS = new HashSet<String>(Arrays.asList(
			"/",
			"/index.jsp",
			"/404.jsp",
			"/500.jsp",
			"/favicon.ico",
			"/LoginServlet"
	));
	
	private static final Set<String> DEFAULT_PUBLIC_PREFIXES = new HashSet<String>(Arrays.asList(
			"/h-ui/",
			"/easyui/"
	));
	
	private static final Set<String> STATIC_RESOURCE_SUFFIXES = new HashSet<String>(Arrays.asList(
			".css",
			".js",
			".png",
			".jpg",
			".jpeg",
			".gif",
			".ico",
			".svg",
			".woff",
			".woff2",
			".ttf",
			".eot"
	));
	
	private final Set<String> publicPaths = new HashSet<String>();
	private final Set<String> publicPrefixes = new HashSet<String>();

	public void destroy() {
		
	}

	public void doFilter(ServletRequest req, ServletResponse rep, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) rep;
		HttpSession session = request.getSession(false);
		User user = session == null ? null : (User) session.getAttribute("user");
		String contextPath = request.getContextPath();
		String requestPath = getRequestPath(request);
		
		if(user != null || isPublicRequest(requestPath)){
			chain.doFilter(request, response);
		} else{
			response.sendRedirect(contextPath+"/index.jsp");
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		publicPaths.clear();
		publicPaths.addAll(DEFAULT_PUBLIC_PATHS);
		publicPrefixes.clear();
		publicPrefixes.addAll(DEFAULT_PUBLIC_PREFIXES);
		loadWhitelist(filterConfig.getInitParameter("publicPaths"), publicPaths);
		loadWhitelist(filterConfig.getInitParameter("publicPrefixes"), publicPrefixes);
	}
	
	private String getRequestPath(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		String requestPath = uri;
		if(contextPath != null && contextPath.length() > 0 && uri.startsWith(contextPath)){
			requestPath = uri.substring(contextPath.length());
		}
		int pathParamIndex = requestPath.indexOf(';');
		if(pathParamIndex != -1){
			requestPath = requestPath.substring(0, pathParamIndex);
		}
		if(requestPath.length() == 0){
			return "/";
		}
		return requestPath;
	}
	
	private boolean isPublicRequest(String requestPath) {
		if(publicPaths.contains(requestPath)){
			return true;
		}
		for(String publicPrefix : publicPrefixes){
			if(requestPath.startsWith(publicPrefix)){
				return true;
			}
		}
		for(String suffix : STATIC_RESOURCE_SUFFIXES){
			if(requestPath.endsWith(suffix)){
				return true;
			}
		}
		return false;
	}
	
	private void loadWhitelist(String configValue, Set<String> target) {
		if(configValue == null || configValue.trim().length() == 0){
			return;
		}
		String[] values = configValue.split(",");
		for(String value : values){
			String path = value.trim();
			if(path.length() > 0){
				target.add(path);
			}
		}
	}

}
