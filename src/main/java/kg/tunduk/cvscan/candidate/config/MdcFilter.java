package kg.tunduk.cvscan.candidate.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcFilter implements Filter {

    private static final String REQUEST_ID = "requestId";
    private static final String X_REQUEST_ID = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestId = httpRequest.getHeader(X_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(REQUEST_ID, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID);
        }
    }
}
