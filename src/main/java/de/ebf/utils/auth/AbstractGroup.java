/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth;

import java.io.Serializable;

/**
 *
 * @author Dominik
 */
public abstract class AbstractGroup<User> implements Group, Serializable {

    private static final long serialVersionUID = 1L;

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
