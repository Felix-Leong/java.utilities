/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.test;

import de.ebf.utils.image.GeneratePreviewTask;
import de.ebf.test.util.TestUtil;
import de.ebf.utils.image.ImageSize;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author dominik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public class GeneratePreviewTest {
    
    private static final Logger log = Logger.getLogger(GeneratePreviewTest.class);
    private static final ImageSize imageSize = new ImageSize(200.0, 200.0);
    private static final File outDir = new File(System.getProperty("java.io.tmpdir"));
    
    private GeneratePreviewTask getTask(String... extensions) throws IOException {
        List<File> files = new ArrayList<>();
        for (String extension: extensions){
            files.add(new ClassPathResource("test."+extension).getFile());
        }
        GeneratePreviewTask task = new GeneratePreviewTask.Builder()
            .withInputFiles(files)
            .withOutputDir(outDir)
            .withImageSize(imageSize)
            .build();
        return task;
    }
    
    @Test
    public void generateJpgPreview() throws IOException{
        log.info("Running jpg test");
        getTask("jpeg").scheduleForSynchronousExecution();
    }
    
    @Test
    public void generatePngPreview() throws IOException{
        log.info("Running png test");
        getTask("png").scheduleForSynchronousExecution();
    }
    
    @Test
    public void generateBmpPreview() throws IOException{
        log.info("Running bmp test");
        getTask("bmp").scheduleForSynchronousExecution();
    }
    
    @Test
    public void generateGifPreview() throws IOException{
        log.info("Running gif test");
        getTask("gif").scheduleForSynchronousExecution();
    }
    
    @Test
    public void generatePdfPreview() throws IOException{
        log.info("Running pdf test");
        getTask("pdf").scheduleForSynchronousExecution();
    }
    
    @Test
    public void generateMixedPreview() throws IOException{
        log.info("Running mixed test");
        getTask("jpeg", "png", "bmp", "gif", "pdf").scheduleForSynchronousExecution();
    }
    
    @Test
    public void generateDocPreview() throws IOException{
        TestUtil.assertWindowsOnly();
        log.info("Running doc test");
        getTask("doc").scheduleForSynchronousExecution();
    }
    
    @Test
    public void generateDocxPreview() throws IOException{
        TestUtil.assertWindowsOnly();
        log.info("Running docx test");
        getTask("docx").scheduleForSynchronousExecution();
    }
    
    @Test
    public void generateXlsPreview() throws IOException{
        TestUtil.assertWindowsOnly();
        log.info("Running xls test");
        getTask("xls").scheduleForSynchronousExecution();
    }
    
    @Test
    public void generateXlsxPreview() throws IOException{
        TestUtil.assertWindowsOnly();
        log.info("Running xlsx test");
        getTask("xlsx").scheduleForSynchronousExecution();
    }
    
    @Test
    public void generatePptPreview() throws IOException{
        TestUtil.assertWindowsOnly();
        log.info("Running ppt test");
        getTask("ppt").scheduleForSynchronousExecution();
    }
    @Test
    public void generatePptxPreview() throws IOException{
        TestUtil.assertWindowsOnly();
        log.info("Running pptx test");
        getTask("pptx").scheduleForSynchronousExecution();
    }
}
