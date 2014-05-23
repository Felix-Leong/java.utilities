package de.ebf.utils.auth;

import java.io.Serializable;

/**
 *
 * @author dwissk
 */
public class AbstractUser implements User, Serializable {
   
   private static final long serialVersionUID = 1L;

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
