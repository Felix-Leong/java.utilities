/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.test;

import de.ebf.office.Office2PDF;
import de.ebf.office.Office2PDFInterface;
import java.io.File;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Dominik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public class Office2PDFTest {
    
    private static final Logger log = Logger.getLogger(Office2PDFTest.class);
    private static Office2PDFInterface office2PDF;
    
    private static final String CONTENT_TYPE_PDF = "application/pdf";
    
    @BeforeClass
    public static void setUp() {
        office2PDF = new Office2PDF();
    }
    
    @Before
    public void windowsOnly() {
        String os = System.getProperty("os.name");
        org.junit.Assume.assumeTrue(os.startsWith("Windows"));
    }
    
    @Test
    public void testDoc() throws Exception{
        test("doc");
    }
    
    @Test
    public void testXls() throws Exception{
        test("xls");
    }
    
    @Test
    public void testPpt() throws Exception{
        test("ppt");
    }
    
    @Test
    public void testDocx() throws Exception{
        test("docx");
    }
    
    @Test
    public void testXlsx() throws Exception{
        test("xlsx");
    }
    
    @Test
    public void testPptx() throws Exception{
        test("pptx");
    }
    
    private void test(String extension) throws Exception{
        log.info("Converting "+extension+" to pdf");
        File input = new ClassPathResource("test."+extension).getFile();
        File output = office2PDF.convert2PDF(input);
        String contentType = new Tika().detect(output);
        assertEquals(contentType, CONTENT_TYPE_PDF);
    }
}
