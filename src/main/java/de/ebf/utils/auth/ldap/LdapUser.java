package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.AbstractUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author dwissk
 */
public class LdapUser extends AbstractUser implements Comparable<LdapUser> {

   private String DN;
   private String firstName;
   private String lastName;
   private String phone;
   private String UUID;
   private String uid;
   private String password;
   private List<LdapGroup> groups;
   private String context;

   public void setPhone(String phone) {
      this.phone = phone;
   }

   public void setUUID(String UUID) {
      this.UUID = UUID;
   }

   public String getPhone() {
      return phone;
   }

   public String getUUID() {
      return UUID;
   }

   public void setUid(String uid) {
      this.uid = uid;
   }

   public String getUid() {
      return uid;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public String getDN() {
      return DN;
   }

   public void setDN(String DN) {
      this.DN = DN;
   }

   public List<LdapGroup> getGroups() {
      return (groups == null) ? new ArrayList<LdapGroup>() : groups;
   }

   public void setGroups(List<LdapGroup> groups) {
      this.groups = groups;
   }

   public String getContext() {
      return context;
   }

   public void setContext(String context) {
      this.context = context;
   }

   public String getTenantID() {
      return LdapUtil.getCN(context);
   }

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public String getLastName() {
      return lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 73 * hash + Objects.hashCode(this.DN);
      hash = 73 * hash + Objects.hashCode(this.UUID);
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
      final LdapUser other = (LdapUser) obj;
      if (!Objects.equals(this.DN, other.DN)) {
         return false;
      }
      if (!Objects.equals(this.UUID, other.UUID)) {
         return false;
      }
      return true;
   }

   @Override
   public int compareTo(LdapUser o) {
      return super.getName().compareTo(o.getName());
   }
}
