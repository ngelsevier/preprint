package com.ssrn.frontend.website;

import org.apache.commons.io.FilenameUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HttpCacheControlFilter implements Filter {
    private static final String[] CACHE_FILE_TYPES = {"eot","css","ttf","woff","gif","svg","js","png"};
    private static final List<String> CACHE_FILE_TYPES_LIST = Arrays.asList(CACHE_FILE_TYPES);

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String fileTypeRequested = FilenameUtils.getExtension(httpServletRequest.getRequestURL().toString());

        if (httpServletRequest.getMethod().equals("GET") && CACHE_FILE_TYPES_LIST.contains(fileTypeRequested)) {
            httpServletResponse.setHeader("Cache-Control", "max-age=7200");
        } else {
            httpServletResponse.setHeader("Cache-Control", "no-cache");
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
