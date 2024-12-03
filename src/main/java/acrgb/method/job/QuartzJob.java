/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method.job;

import java.util.Date;
import java.util.TimerTask;
//import java.util.logging.Level;
//import org.apache.log4j.BasicConfigurator;
//import org.quartz.JobBuilder;
//import org.quartz.JobDetail;
//import org.quartz.Scheduler;
//import org.quartz.SchedulerException;
//import org.quartz.SimpleScheduleBuilder;
//import org.quartz.Trigger;
//import org.quartz.TriggerBuilder;
//import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author ACR_GB
 */
public class QuartzJob extends TimerTask {

    Date now;

    @Override
    public void run() {
        now = new Date(); // initialize date
        System.out.println("Time is :" + now); // Display current time
    }

//    private static final String NAME_OF_JOB = "Job1";
//    private static final String NAME_OF_GROUP = "group1";
//    private static final String NAME_OF_TRIGGER = "triggerStart";
//
//    private static Scheduler scheduler;
//
//    public static void JobExecutor() {
//        try {
//            BasicConfigurator.configure();
//
//            //show message to know about the main thread   
//            System.out.println(" The name of the QuartzScheduler main thread is: " + Thread.currentThread().getName());
//
//            //initialize scheduler instance from Quartz
//            scheduler = new StdSchedulerFactory().getScheduler();
//
//            //start scheduler  
//            scheduler.start();
//
//            //create scheduler trigger based on the time interval  
//            Trigger triggerNew = createTrigger();
//
//            //create scheduler trigger with a cron expression  
//            //Trigger triggerNew = createCronTrigger();  
//            //schedule trigger  
//            scheduleJob(triggerNew);
//
//        } catch (SchedulerException ex) {
//            java.util.logging.Logger.getLogger(QuartzJob.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            java.util.logging.Logger.getLogger(QuartzJob.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    //create scheduleJob() method to schedule a job  
//    private static void scheduleJob(Trigger triggerNew) throws Exception {
//
//        //create an instance of the JoDetails to connect Quartz job to the CreateQuartzJob  
//        JobDetail jobInstance = JobBuilder.newJob(null).withIdentity(NAME_OF_JOB, NAME_OF_GROUP).build();
//
//        //invoke scheduleJob method to connect the Quartz scheduler to the jobInstance and the triggerNew  
//        scheduler.scheduleJob(jobInstance, triggerNew);
//
//    }
//
//    //create createTrigger() method that returns a trigger based on the time interval  
//    private static Trigger createTrigger() {
//
//        //initialize time interval  
//        int TIME_INTERVAL = 60;
//
//        //create a trigger to be returned from the method  
//        Trigger triggerNew = TriggerBuilder.newTrigger().withIdentity(NAME_OF_TRIGGER, NAME_OF_GROUP)
//                .withSchedule(
//                        SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(TIME_INTERVAL).repeatForever())
//                .build();
//
//        // triggerNew to schedule it in main() method  
//        return triggerNew;
//    }
}
