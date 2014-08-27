/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.scheduling;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jboss.logging.Logger;

/**
 *
 * @author dominik
 */
public class TaskScheduler {
    
    private static final Logger log = Logger.getLogger(TaskScheduler.class);
    
    private static final int MAX_CONCURRENT_THREADS = 5;
    
    private static ScheduledExecutorService scheduledExecutorService;
    
    public void scheduleForImmediateExecution(List<Callable> callables) throws Exception{
        scheduleAtFixedRate(callables, null, null);
    }
    
    public void scheduleAtFixedRate(List<Callable> callables, Date startDate, Long intervalInMillis) throws Exception{
        initScheduler();
        Long delay = 0L;
        Date now = new Date();
        if (startDate!= null && startDate.after(now)){
            delay = startDate.getTime() - now.getTime(); 
        }
        String startDateStr = (startDate == null) ? "now" : "at "+startDate;
        log.info("Scheduling "+callables.size()+" tasks starting "+startDateStr);
        for (Callable callable: callables){
            if (intervalInMillis==null){
                scheduledExecutorService.schedule(callable, delay, TimeUnit.MILLISECONDS);
            } else {
                Runnable runnable = new SilentRunnable(callable);
                scheduledExecutorService.scheduleAtFixedRate(runnable, delay, intervalInMillis, TimeUnit.MILLISECONDS);
            }
        }
    }
    
    private void initScheduler() {
        if (scheduledExecutorService!=null){
            scheduledExecutorService.shutdown();
        }
        
        scheduledExecutorService = Executors.newScheduledThreadPool(MAX_CONCURRENT_THREADS);
    }
    
    private static class SilentRunnable implements Runnable {
        
        private final Callable callable;
        
        public SilentRunnable(Callable callable){
            this.callable = callable;
        }

        @Override
        public void run() {
            try {
                callable.call();
            } catch (Exception ex) {
                log.error(ex);
            }
        }
    }


}
