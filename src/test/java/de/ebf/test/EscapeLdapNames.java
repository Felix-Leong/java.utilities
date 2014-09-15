/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.test;

import de.ebf.utils.auth.ldap.LdapUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author dominik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public class EscapeLdapNames {

    private static Map<String, String> testMap;
    
    public EscapeLdapNames() {
    }

    @BeforeClass
    public static void setUpClass() {
        testMap = new HashMap();
        testMap.put("test#1", "test\\#1");
        testMap.put("test'", "test\\'");
        testMap.put("test!", "test\\!");
        testMap.put("test\"", "test\\\"");
        testMap.put("Test$1", "Test\\$1");
        testMap.put("test%", "test\\%");
        testMap.put("test&", "test\\&");
        testMap.put("test(", "test\\(");
        testMap.put("test)", "test\\)");
        testMap.put("test*", "test\\*");
        testMap.put("test+", "test\\+");
        testMap.put("test.", "test\\.");
        testMap.put("test/a", "test\\/a");
        testMap.put("test:", "test\\:");
        testMap.put("test<", "test\\<");
        testMap.put("test=", "test\\=");
        testMap.put("test>", "test\\>");
        testMap.put("test?", "test\\?");
        testMap.put("test^", "test\\^");
        testMap.put("Test_1", "Test\\_1");
        testMap.put("test{", "test\\{");
        testMap.put("test|", "test\\|");
        testMap.put("test}", "test\\}");
        testMap.put("test~", "test\\~");
        testMap.put("$#!7", "\\$\\#\\!7");
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test() {
        for (Entry<String, String> entry: testMap.entrySet()){
            String output = LdapUtil.escapeCN(entry.getKey());
            Assert.assertEquals(output, entry.getValue());
        }
    }
}
