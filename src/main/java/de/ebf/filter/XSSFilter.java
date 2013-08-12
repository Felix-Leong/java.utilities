/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Dominik
 */
public class XSSFilter implements Filter {

   @Override
   public void init(FilterConfig filterConfig) throws ServletException {
   }

   @Override
   public void destroy() {
   }

   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
      if (request instanceof HttpServletRequest) {
         chain.doFilter(new XSSRequestWrapper((HttpServletRequest) request), response);
      } else {
         chain.doFilter(request, response);
      }
   }
}
