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

import com.lizhou.bean.User;

public class VisitFilter implements Filter {

	private static final Set<String> WHITE_PAGES = new HashSet<>(Arrays.asList(
		"/index.jsp",
		"/404.jsp",
		"/500.jsp"
	));

	private static final Set<String> STATIC_EXTENSIONS = new HashSet<>(Arrays.asList(
		".css", ".js", ".jpg", ".jpeg", ".png", ".gif", ".ico",
		".woff", ".woff2", ".ttf", ".svg", ".eot", ".otf", ".map"
	));

	public void destroy() {

	}

	public void doFilter(ServletRequest req, ServletResponse rep, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) rep;

		String contextPath = request.getContextPath();
		String uri = request.getRequestURI();

		String path = uri.substring(contextPath.length());

		if (isWhitePage(path) || isStaticResource(path)) {
			chain.doFilter(request, response);
			return;
		}

		User user = (User) request.getSession().getAttribute("user");

		if (user != null) {
			chain.doFilter(request, response);
		} else {
			response.sendRedirect(contextPath + "/index.jsp");
		}
	}

	private boolean isWhitePage(String path) {
		return WHITE_PAGES.contains(path);
	}

	private boolean isStaticResource(String path) {
		if (path == null || path.isEmpty()) {
			return false;
		}
		String lower = path.toLowerCase();
		for (String ext : STATIC_EXTENSIONS) {
			if (lower.endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	public void init(FilterConfig arg0) throws ServletException {

	}

}
