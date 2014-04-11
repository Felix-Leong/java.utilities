/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.onpremise;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 *
 * This adapted class checks whether the root application context has been started before the filter job.
 * If the root application context has not been started, it does not do the filter job. Otherwise, it does.
 * Because the FilterBean need to be injected with DAO beans, which is created and managed by the root application context.
 * @author xz
 */
public class OnpremiseDelegatingFilterProxy extends DelegatingFilterProxy {
    private static final Logger log = Logger.getLogger(OnpremiseDelegatingFilterProxy.class);

    /**
     * You don't have to call initFilterBean() in the doFilter() method when the root application context is started after the init() method. 
     * Because, if you look into the source code of DelegatingFilterProxy.java, you will see the doFilter() method do the same thing.

     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (existRootApplicationContext()) {
            super.doFilter(request, response, filterChain);
        } else {
            filterChain.doFilter(request, response);
        }

    }

    /**
     * To check if the root application context has been started.
     * @return 
     */
    boolean existRootApplicationContext() {
        WebApplicationContext wac = findWebApplicationContext();
        return (wac != null);
    }
}
