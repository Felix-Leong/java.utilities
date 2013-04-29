package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.AbstractUser;

/**
 *
 * @author dwissk
 */
public class LdapUser extends AbstractUser {

   private String phone;
   private String UUID;
   private String userDN;
   private String uid;

   void setPhone(String phone) {
      this.phone = phone;
   }

   void setUUID(String UUID) {
      this.UUID = UUID;
   }

   void setUserDN(String userDN) {
      this.userDN = userDN;
   }

   public String getPhone() {
      return phone;
   }

   public String getUUID() {
      return UUID;
   }

   public String getUserDN() {
      return userDN;
   }

   void setUid(String uid) {
      this.uid = uid;
   }

   public String getUid() {
      return uid;
   }
}
