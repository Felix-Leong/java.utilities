/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.AbstractGroup;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Dominik
 */
public class LdapGroup extends AbstractGroup implements Comparable<LdapGroup>, Serializable {
   
   private static final long serialVersionUID = 1L;

   private String UUID;
   private String DN;
   private List<String> memberDNs;
   private List<LdapUser> members;
   private String context;
   
   //Active Directory only
   private String objectSid;

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
   
    public List<String> getMemberDNs() {
        return (memberDNs == null) ? new ArrayList<String>() : memberDNs;
    }

    public void setMemberDNs(List<String> memberDNs) {
        this.memberDNs = memberDNs;
    }

   public List<LdapUser> getMembers() {
      return (members == null) ? new ArrayList<LdapUser>() : members;
   }

   public void setMembers(List<LdapUser> users) {
      this.members = users;
   }

   public String getContext() {
      return context;
   }

   public void setContext(String context) {
      this.context = context;
   }

    public String getObjectSid() {
        return objectSid;
    }

    public void setObjectSid(String objectSid) {
        this.objectSid = objectSid;
    }
   
   @Override
   public int compareTo(LdapGroup o) {
      int result = this.getName().compareTo(o.getName());
      if (result == 0){
          //if the names are the same but the UUID is different do not return 0
          if (this.UUID!=null && o.UUID!=null && !this.UUID.equals(o.UUID)){
              return 1;
          }
      }
      return result;
   }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.UUID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LdapGroup o = (LdapGroup) obj;
        return this.UUID!=null && o.UUID!=null && this.UUID.equals(o.UUID);
    }



}
