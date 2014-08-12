package de.ebf.filter;

import de.ebf.utils.HttpUtil;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

public class LoginFilter implements Filter {

    private static final Logger log = Logger.getLogger(LoginFilter.class);
    private Pattern exclusionPattern;
    private String contextPath;
    //This filter should not do the job if the MainDispatcher is not working.
    private static boolean isMainDispatcher = true;

    @Override
    public void init(FilterConfig config) throws ServletException {
        String exclusions = config.getInitParameter("exclusions");
        try {
            exclusionPattern = Pattern.compile(exclusions);
        } catch (PatternSyntaxException e) {
            throw new RuntimeException("Could not compile LoginFilter exlusion pattern [" + exclusions + "]", e);
        }
    }

    @Override
    public void destroy() {
        //empty
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            contextPath = HttpUtil.getContextPath(httpRequest);
            request.setAttribute("contextPath", contextPath);

            if (!isMainDispatcher) {
                filterChain.doFilter(request, response);
                return;
            }

            HttpSession session = httpRequest.getSession();
            String servletPath = httpRequest.getServletPath();
            if (!exclusionPattern.matcher(servletPath).matches()) {
                if (session == null) {
                    log.info("Redirecting [" + servletPath + "] to login page due to empty session.");
                    redirectToLogin(response);
                } else {
                    Object user = session.getAttribute("user");
                    if (user == null) {
                        String sessionRequestURI = (String) session.getAttribute("requestURI");
                        if (sessionRequestURI == null) {
                            String requestURI = httpRequest.getRequestURI();
                            int index = requestURI.indexOf(contextPath);
                            if (index != -1) {
                                requestURI = requestURI.substring(index + contextPath.length());
                            }
                            httpRequest.getSession().setAttribute("requestURI", requestURI);
                        }
                        log.info("Redirecting [" + servletPath + "] to login page due to missing user session object");
                        redirectToLogin(response);
                        return;
                    }
                }
            }
            filterChain.doFilter(request, response);
        }
    }

    private void redirectToLogin(ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.sendRedirect(contextPath + "/login");
    }
    
    public static void setMainDispatcher(boolean isMainDispatcher){
        LoginFilter.isMainDispatcher = isMainDispatcher;
    }
}
