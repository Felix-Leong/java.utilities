package de.ebf.taglib;

import com.squeakysand.jsp.tagext.EnhancedSimpleTagSupport;
import com.squeakysand.jsp.tagext.annotations.JspTag;
import java.io.IOException;
import java.util.Date;
import javax.servlet.jsp.JspException;

@JspTag
public class HumanReadableDurationTag extends EnhancedSimpleTagSupport {

   private Date start;
   private Date end;

   public Date getStart() {
      return start;
   }

   public void setStart(Date start) {
      this.start = start;
   }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
   
   @Override
   public void doTag() throws JspException, IOException {
      if (start!=null && end!=null){
          long diffMillis = end.getTime() - start.getTime();
          if (diffMillis<1000){
            getJspWriter().write("<1s");
          } else {
              getJspWriter().write(diffMillis/1000 +"s");
          }
      } else {
           getJspWriter().write("");
      }
   }
}
