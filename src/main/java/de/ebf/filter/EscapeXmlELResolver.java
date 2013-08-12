/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.filter;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.servlet.jsp.JspContext;

/**
 *
 * @author Dominik
 */
public class EscapeXmlELResolver extends ELResolver {

   /**
    * pageContext attribute name for flag to enable XML escaping
    */
   static final String ESCAPE_XML_ATTRIBUTE =
           EscapeXmlELResolver.class.getName() + ".escapeXml";
   private ThreadLocal<Boolean> excludeMe = new ThreadLocal<Boolean>() {
      @Override
      protected Boolean initialValue() {
         return Boolean.FALSE;
      }
   };

   @Override
   public Class<?> getCommonPropertyType(ELContext context, Object base) {
      return null;
   }

   @Override
   public Iterator<FeatureDescriptor> getFeatureDescriptors(
           ELContext context, Object base) {
      return null;
   }

   @Override
   public Class<?> getType(ELContext context, Object base, Object property) {
      return null;
   }

   @Override
   public Object getValue(ELContext context, Object base, Object property) {
      JspContext pageContext = (JspContext) context.getContext(JspContext.class);
      Boolean escapeXml = (Boolean) pageContext.getAttribute(ESCAPE_XML_ATTRIBUTE);
      if (escapeXml != null && !escapeXml) {
         return null;
      }

      try {
         if (excludeMe.get()) {
            return null;
         }

         // This resolver is in the original resolver chain. To prevent
         // infinite recursion, set a flag to prevent this resolver from
         // invoking the original resolver chain again when its turn in the
         // chain comes around.
         excludeMe.set(Boolean.TRUE);
         Object value = context.getELResolver().getValue(
                 context, base, property);

         if (value instanceof String) {
            value = EscapeXml.escape((String) value);
         }
         return value;

      } finally {
         excludeMe.remove();
      }
   }

   @Override
   public boolean isReadOnly(ELContext context, Object base, Object property) {
      return true;
   }

   @Override
   public void setValue(
           ELContext context, Object base, Object property, Object value) {
   }
}
