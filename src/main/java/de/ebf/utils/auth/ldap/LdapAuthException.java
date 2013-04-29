package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.AuthException;

/**
 *
 * @author dwissk
 */
public class LdapAuthException extends AuthException {

   LdapAuthException(String cause) {
      super(cause);
   }

   LdapAuthException(Exception e) {
      super(e);
   }
}
