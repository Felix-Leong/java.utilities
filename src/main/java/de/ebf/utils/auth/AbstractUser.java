package de.ebf.utils.auth;

/**
 *
 * @author dwissk
 */
public class AbstractUser implements User {

   private String name;
   private String mail;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getMail() {
      return mail;
   }

   public void setMail(String mail) {
      this.mail = mail;
   }
}
