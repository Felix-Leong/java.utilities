package de.ebf.taglib;

import com.squeakysand.jsp.tagext.EnhancedSimpleTagSupport;
import com.squeakysand.jsp.tagext.annotations.JspTag;
import de.ebf.utils.FormatUtils;
import java.io.IOException;
import javax.servlet.jsp.JspException;

@JspTag
public class HumanReadableSizeTag extends EnhancedSimpleTagSupport {

   private Long value;

   public Long getValue() {
      return value;
   }

   public void setValue(Long value) {
      this.value = value;
   }

   @Override
   public void doTag() throws JspException, IOException {
      String out = FormatUtils.readableFileSize(value);
      getJspWriter().write(out);
   }
}
