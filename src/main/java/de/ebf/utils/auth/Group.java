/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth;

import java.util.List;

/**
 *
 * @author Dominik
 */
public interface Group {
   
   public String getName();
   
   public void setName(String name);
   
   public List<? extends User> getMembers();
   
   public void setMembers(List<? extends User> users);
}
