package de.ebf.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author dwissk
 */
@Controller
@RequestMapping("/error")
public class HttpErrorController {

   @RequestMapping(value = "{errorCode}", method = RequestMethod.GET)
   public ModelAndView index(HttpServletRequest request, HttpServletResponse response, @PathVariable Long errorCode) {
      return new ModelAndView("error/error", "ErrorCode", errorCode);
   }
   
   public static ModelAndView showError(HttpServletRequest request, HttpServletResponse response, Exception e) {
      ModelAndView mav = new ModelAndView("error/error", "ErrorCode", 500);
      mav.addObject("exception", e);
      return mav;
   }
}
