package com.ssrn.fake_old_platform;

import javax.servlet.*;
import java.io.IOException;
import java.util.Queue;

public class NextRequestDelayFilter implements javax.servlet.Filter {
    private final Queue<ResponseDelay> responseDelays;

    public NextRequestDelayFilter(Queue<ResponseDelay> responseDelays) {
        this.responseDelays = responseDelays;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ResponseDelay responseDelay = this.responseDelays.poll();
        if (responseDelay != null) {
            try {
                Thread.sleep(responseDelay.getMilliseconds());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
