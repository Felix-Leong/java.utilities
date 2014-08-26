/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.utils.image;

import de.ebf.scheduling.TaskScheduler;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tika.io.IOUtils;

/**
 *
 * @author dominik
 */
public class GeneratePreviewTask{
    
    private List<File> files;
    private File outputDir;
    private ImageSize imageSize;
    private static TaskScheduler scheduler;
    private ProgressCallback callback;
    
    //hide constructor, use builder pattern
    private GeneratePreviewTask(){};
    
    public void scheduleForImmediateExecution() throws Exception{
        scheduleAtFixedRate(null, null);
    }
    
    public void scheduleAtFixedRate(Date startDate, Long intervalInMillis) throws Exception{
        if (scheduler == null){
            scheduler = new TaskScheduler();
        }
        List<Callable> callables = new ArrayList<>();
        for (File file: files){
            callables.add(new GeneratePreviewCallable(file, imageSize, outputDir));
        }
        scheduler.scheduleAtFixedRate(callables, startDate, intervalInMillis);
    }
    
    private class GeneratePreviewCallable implements Callable{
        
        private final File file;
        private final ImageSize imageSize;
        private final File outputDir;        

        private GeneratePreviewCallable(File file, ImageSize imageSize, File outputDir){
            this.file = file;
            this.outputDir = outputDir;
            this.imageSize = imageSize;
        }

        @Override
        public Object call() throws Exception {
            byte[] previewImage = ImageUtil.getPreviewImageByteArray(file, imageSize);
            String fileHash = DigestUtils.shaHex(IOUtils.toByteArray(new FileInputStream(file)));
            File previewImageFile = new File(outputDir, fileHash);
            FileUtils.writeByteArrayToFile(previewImageFile, previewImage);
            return previewImageFile;
        }   
    }
    
    
    private void setFiles(List<File> files) {
        this.files = files;
    }
    
    private void setOutputDir(File outputDir){
        this.outputDir = outputDir;
    }

    private void setImageSize(ImageSize imageSize) {
        this.imageSize = imageSize;
    }

    private void setCallback(ProgressCallback callback) {
        this.callback = callback;
    }
    
    public static class Builder{
        
        private List<File> files;
        private File outputDir;
        private ImageSize imageSize;
        private ProgressCallback callback;

        public Builder withInputFiles(List<File> files){
            this.files = files;
            return this;
        }

        public Builder withOutputDir(File dir){
            if (!dir.isDirectory()){
                throw new IllegalArgumentException();
            }
            this.outputDir = dir;
            return this;
        }

        public Builder withImageSize(ImageSize imageSize){
            this.imageSize = imageSize;
            return this;
        }

        public Builder withProgressCallback(ProgressCallback callback){
            this.callback = callback;
            return this;
        }
        
        public GeneratePreviewTask build(){
            GeneratePreviewTask task = new GeneratePreviewTask();
            if (files == null || outputDir == null || imageSize == null){
                throw new IllegalArgumentException();
            }
            task.setFiles(files);
            task.setOutputDir(outputDir);
            task.setImageSize(imageSize);
            task.setCallback(callback);
            return task;
        }
    }
}
