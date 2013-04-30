package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.AbstractUser;

/**
 *
 * @author dwissk
 */
public class LdapUser extends AbstractUser {

   private String DN;
   private String phone;
   private String UUID;
   private String uid;
   private String password;

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
}
