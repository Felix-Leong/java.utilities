package de.ebf.taglib;

import com.squeakysand.jsp.tagext.EnhancedSimpleTagSupport;
import com.squeakysand.jsp.tagext.annotations.JspTag;
import java.io.IOException;
import java.util.Date;
import javax.servlet.jsp.JspException;
import org.ocpsoft.prettytime.PrettyTime;

@JspTag
public class HumanReadableTimeTag extends EnhancedSimpleTagSupport {

   private Date value;

   public Date getValue() {
      return value;
   }

   public void setValue(Date value) {
      this.value = value;
   }

   @Override
   public void doTag() throws JspException, IOException {
      PrettyTime pt = new PrettyTime();
      getJspWriter().write(pt.format(value));
   }
}
