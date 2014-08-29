/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.test.util;

/**
 *
 * @author Dominik
 */
public class TestUtil {

    public static void assertWindowsOnly() {
        String os = System.getProperty("os.name");
        org.junit.Assume.assumeTrue(os.startsWith("Windows"));
    }
    
}
