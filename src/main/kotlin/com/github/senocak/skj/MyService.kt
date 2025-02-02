package com.github.senocak.skj

import org.jobrunr.jobs.annotations.Job
import org.jobrunr.jobs.annotations.Recurring
import org.jobrunr.jobs.context.JobContext
import org.jobrunr.jobs.context.JobDashboardProgressBar
import org.jobrunr.jobs.context.JobRunrDashboardLogger
import org.slf4j.Logger
import org.springframework.stereotype.Component

@Component
class MyService {
    private val log: Logger by logger()
    private val logger = JobRunrDashboardLogger(log)

    @Recurring(id = "my-recurring-job", cron = "0 0/15 * * *")
    @Job(name = "My recurring job")
    fun doRecurringJob() {
        log.info("Doing some work without arguments")
    }

    fun doSimpleJob(anArgument: String) {
        log.info("Doing some work: $anArgument")
    }

    fun doLongRunningJob(anArgument: String) {
        try {
            (0..9).forEach { i: Int ->
                log.info("Doing work item $i: $anArgument")
                logger.info("Doing work item $i: $anArgument")
                Thread.sleep(20_000)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    fun doLongRunningJobWithJobContext(anArgument: String, jobContext: JobContext) {
        try {
            log.warn("Starting long running job...")
            val progressBar: JobDashboardProgressBar = jobContext.progressBar(10)
            (0..9).forEach { i: Int ->
                log.info("Processing item $i: $anArgument")
                logger.info("Processing item $i: $anArgument")
                Thread.sleep(5_000)
                progressBar.increaseByOne()
            }
            log.warn("Finished long running job...")
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}