package org.flinkcoin.node.jobs;

import org.flinkcoin.node.managers.ConnectionManager;
import com.google.inject.Singleton;
import javax.inject.Inject;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@DisallowConcurrentExecution
public class ConnectionJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionJob.class);
    private final ConnectionManager discoveryService;

    @Inject
    public ConnectionJob(ConnectionManager discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("query!");
        discoveryService.query();
    }
}
