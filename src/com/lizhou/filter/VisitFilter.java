package com.lizhou.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lizhou.bean.User;

public class VisitFilter implements Filter {

	private static final List<String> WHITE_LIST = Arrays.asList(
		"/index.jsp",
		"/404.jsp",
		"/500.jsp",
		"/css/",
		"/js/",
		"/images/",
		"/static/"
	);

	public void destroy() {
		
	}

	public void doFilter(ServletRequest req, ServletResponse rep, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) rep;
		
		User user = (User) request.getSession().getAttribute("user");
		
		String contextPath = request.getContextPath();
		
		String uri = request.getRequestURI();
		String path = uri.substring(contextPath.length());
		
		boolean isWhiteListed = false;
		for (String whitePath : WHITE_LIST) {
			if (path.startsWith(whitePath) || path.equals(whitePath)) {
				isWhiteListed = true;
				break;
			}
		}
		
		if (isWhiteListed || user != null) {
			chain.doFilter(request, response);
		} else {
			response.sendRedirect(contextPath + "/index.jsp");
		}
	}

	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
