package de.ebf.taglib;

import com.squeakysand.jsp.tagext.EnhancedSimpleTagSupport;
import com.squeakysand.jsp.tagext.annotations.JspTag;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.jsoup.Jsoup;

@JspTag
public class StripHtmlTag extends EnhancedSimpleTagSupport {

   private String value;

   public String getValue() {
      return value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   @Override
   public void doTag() throws JspException, IOException {
      String out = Jsoup.parse(value).text();
      getJspWriter().write(out);
   }
}
