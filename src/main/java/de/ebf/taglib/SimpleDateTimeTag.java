package de.ebf.taglib;

import com.squeakysand.jsp.tagext.EnhancedSimpleTagSupport;
import com.squeakysand.jsp.tagext.annotations.JspTag;
import de.ebf.utils.FormatUtils;
import java.io.IOException;
import java.util.Date;
import javax.servlet.jsp.JspException;

@JspTag
public class SimpleDateTimeTag extends EnhancedSimpleTagSupport {

   private Date value;

   public Date getValue() {
      return value;
   }

   public void setValue(Date value) {
      this.value = value;
   }

   @Override
   public void doTag() throws JspException, IOException {
      getJspWriter().write(FormatUtils.DATETIME_FORMAT_METRIC.format(value));
   }
}
