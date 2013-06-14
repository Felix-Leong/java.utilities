/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.AbstractGroup;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dominik
 */
public class LdapGroup extends AbstractGroup implements Comparable<LdapGroup> {

   private String UUID;
   private String DN;
   private List<LdapUser> members;

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

   public List<LdapUser> getMembers() {
      return (members == null) ? new ArrayList<LdapUser>() : members;
   }

   public void setMembers(List<LdapUser> users) {
      this.members = users;
   }
}
