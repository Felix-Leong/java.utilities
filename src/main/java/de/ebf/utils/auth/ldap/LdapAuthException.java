package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.AuthException;

/**
 *
 * @author dwissk
 */
public class LdapAuthException extends AuthException {

   public LdapAuthException(String cause) {
      super(cause);
   }

   public LdapAuthException(Exception e) {
      super(e);
   }
}
