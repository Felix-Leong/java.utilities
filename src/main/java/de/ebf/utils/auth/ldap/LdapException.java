package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.AuthException;

/**
 *
 * @author dwissk
 */
public class LdapException extends AuthException {

   public LdapException(String cause) {
      super(cause);
   }

   public LdapException(Exception e) {
      super(e);
   }
}
