package com.flick.node.jobs;

import com.flick.node.configuration.guice.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JobsInit {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobsInit.class);

    private final Scheduler scheduler;

    @Inject
    public JobsInit(final GuiceJobFactory guiceJobFactory) throws SchedulerException {
        scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.setJobFactory(guiceJobFactory);
    }

    public void start() throws SchedulerException {
        LOGGER.info("Starting jobs!");
        this.scheduler.start();

        initConnectionJob();
        initUnclaimedBlockJob();
    }

    private void initUnclaimedBlockJob() throws SchedulerException {
        JobDetail jobDetail = newJob(UnclaimedBlockJob.class)
                .build();

        Trigger trigger = newTrigger()
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(2).repeatForever()
                )
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    private void initConnectionJob() throws SchedulerException {
        JobDetail jobDetail = newJob(ConnectionJob.class)
                .build();

        Trigger trigger = newTrigger()
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(10).repeatForever()
                )
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void stop() throws SchedulerException {
        LOGGER.info("Stoping jobs!");
        this.scheduler.shutdown(true);
    }
}
