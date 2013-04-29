package de.ebf.utils.auth;

/**
 *
 * @author dwissk
 */
public class AuthException extends Exception {

   public AuthException(String cause) {
      super(cause);
   }

   public AuthException(Exception e) {
      super(e);
   }
}
