/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.cache;

import com.googlecode.ehcache.annotations.key.HashCodeCacheKeyGenerator;

/**
 *
 * @author dominik
 */
public class NoMethodHashCodeCacheKeyGenerator extends HashCodeCacheKeyGenerator{
    
    public NoMethodHashCodeCacheKeyGenerator(){
        super(false, true);
    }
    
}
