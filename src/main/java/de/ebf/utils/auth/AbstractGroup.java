/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth;

/**
 *
 * @author Dominik
 */
public abstract class AbstractGroup<User> implements Group {

   private String name;

   @Override
   public String getName() {
      return name;
   }

   @Override
   public void setName(String name) {
      this.name = name;
   }
}
