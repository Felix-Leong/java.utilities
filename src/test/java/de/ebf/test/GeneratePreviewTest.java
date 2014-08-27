/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.test;

import de.ebf.utils.image.GeneratePreviewTask;
import de.ebf.utils.image.ImageSize;
import de.ebf.utils.image.ProgressCallback;
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
    public void generateJpgPreview() throws Exception{
        log.info("Running jpg test");
        getTask("jpeg").scheduleForImmediateExecution();
    }
    
    @Test
    public void generatePngPreview() throws Exception{
        log.info("Running png test");
        getTask("png").scheduleForImmediateExecution();
    }
    
    @Test
    public void generateBmpPreview() throws Exception{
        log.info("Running bmp test");
        getTask("bmp").scheduleForImmediateExecution();
    }
    
    @Test
    public void generateGifPreview() throws Exception{
        log.info("Running gif test");
        getTask("gif").scheduleForImmediateExecution();
    }
    
    @Test
    public void generatePdfPreview() throws Exception{
        log.info("Running pdf test");
        getTask("pdf").scheduleForImmediateExecution();
    }
    
    @Test
    public void generateMixedPreview() throws Exception{
        log.info("Running mixed test");
        getTask("jpeg", "png", "bmp", "gif", "pdf").scheduleForImmediateExecution();
    }
    
    private class TestProgressCallback implements ProgressCallback{

        @Override
        public void onProgress(Long total, Long progress) {
            log.info("Finsished "+progress+" of "+total);
        }
        
    }
}
