package de.ebf.utils.auth;

/**
 *
 * @author dwissk
 */
public class AbstractUser implements User {

   private String name;
   private String mail;

   @Override
   public String getName() {
      return name;
   }

   @Override
   public void setName(String name) {
      this.name = name;
   }

   @Override
   public String getMail() {
      return mail;
   }

   @Override
   public void setMail(String mail) {
      this.mail = mail;
   }
}
