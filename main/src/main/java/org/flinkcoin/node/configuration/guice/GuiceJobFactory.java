package org.flinkcoin.node.configuration.guice;

/*-
 * #%L
 * Flink - Node
 * %%
 * Copyright (C) 2021 Flink Foundation
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GuiceJobFactory implements JobFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuiceJobFactory.class);

    private final Injector guice;

    @Inject
    public GuiceJobFactory(final Injector guice) {
        this.guice = guice;
    }

    @Override
    public Job newJob(TriggerFiredBundle tfb, Scheduler schdlr) throws SchedulerException {
        JobDetail jobDetail = tfb.getJobDetail();
        Class<? extends Job> jobClass = jobDetail.getJobClass();

        try {
            // Get a new instance of that class from Guice so we can do dependency injection
            return (Job) guice.getInstance(jobClass);
        } catch (Exception ex) {
            // Something went wrong.  Print out the stack trace here so SLF4J doesn't hide it.
            LOGGER.error("Guice failed to inject dependencies into the job " + jobClass.getName(), ex);

            // Rethrow the exception as an UnsupportedOperationException
            throw new UnsupportedOperationException(ex);
        }
    }

}
