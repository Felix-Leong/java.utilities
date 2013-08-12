/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.filter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.jsp.JspFactory;

/**
 *
 * @author Dominik
 */
public class EscapeXmlELResolverListener implements ServletContextListener {

   @Override
   public void contextInitialized(ServletContextEvent event) {
      JspFactory.getDefaultFactory()
              .getJspApplicationContext(event.getServletContext())
              .addELResolver(new EscapeXmlELResolver());
   }

   @Override
   public void contextDestroyed(ServletContextEvent event) {
   }
}
