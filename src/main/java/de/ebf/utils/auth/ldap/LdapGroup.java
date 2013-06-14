/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.AbstractGroup;

/**
 *
 * @author Dominik
 */
public class LdapGroup extends AbstractGroup implements Comparable<LdapGroup> {

   String UUID;
   String DN;

   public String getUUID() {
      return UUID;
   }

   public void setUUID(String UUID) {
      this.UUID = UUID;
   }

   public String getDN() {
      return DN;
   }

   public void setDN(String DN) {
      this.DN = DN;
   }

   @Override
   public int compareTo(LdapGroup o) {
      return this.getName().compareTo(o.getName());
   }
}
