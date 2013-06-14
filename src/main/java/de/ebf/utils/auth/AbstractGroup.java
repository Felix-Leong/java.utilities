/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dominik
 */
public class AbstractGroup implements Group {

   private String name;
   private List<? extends User> members;

   @Override
   public String getName() {
      return name;
   }

   @Override
   public void setName(String name) {
      this.name = name;
   }

   @Override
   public List<? extends User> getMembers() {
      return (members == null) ? new ArrayList<User>() : members;
   }

   @Override
   public void setMembers(List<? extends User> members) {
      this.members = members;
   }
}
