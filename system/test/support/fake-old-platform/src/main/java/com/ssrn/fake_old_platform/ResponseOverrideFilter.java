package com.ssrn.fake_old_platform;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Queue;

public class ResponseOverrideFilter implements Filter {
    private final Queue<OverriddenResponse> overriddenResponses;

    ResponseOverrideFilter(Queue<OverriddenResponse> overriddenResponses) {
        this.overriddenResponses = overriddenResponses;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        OverriddenResponse overriddenResponse = overriddenResponses.poll();

        if (overriddenResponse != null) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setContentType(overriddenResponse.getContentType());
            httpServletResponse.setStatus(overriddenResponse.getStatusCode());
            httpServletResponse.getWriter().append(overriddenResponse.getBody());
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
