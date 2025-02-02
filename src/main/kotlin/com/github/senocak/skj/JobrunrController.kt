package com.github.senocak.skj

import org.jobrunr.jobs.JobId
import org.jobrunr.jobs.context.JobContext
import org.jobrunr.jobs.context.JobRunrDashboardLogger
import org.jobrunr.scheduling.JobScheduler
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.Instant.now
import java.util.UUID

@RestController
@RequestMapping("/jobs")
class JobrunrController(
    val jobScheduler: JobScheduler,
    val myService: MyService
) {
    private val log: Logger by logger()

    @GetMapping
    fun getAll(): String {
        log.info("Ping")
        return "Ping"
    }

    @GetMapping(value = ["/simple-job"])
    fun simpleJob(@RequestParam(defaultValue = "World") name: String): String {
        val enqueuedJobId: JobId = jobScheduler.enqueue { myService: MyService -> myService.doSimpleJob(anArgument = "Hello $name") }
        return "Job Enqueued: $enqueuedJobId"
    }

    @GetMapping(value = ["/schedule-simple-job"])
    fun scheduleSimpleJob(
        @RequestParam(defaultValue = "Hello world") value: String,
        @RequestParam(defaultValue = "PT3H") `when`: String?
    ): String {
        val scheduledJobId: JobId = jobScheduler.schedule(now().plus(Duration.parse(`when`))) { myService.doSimpleJob(value) }
        return "Job Scheduled: $scheduledJobId"
    }

    @GetMapping(value = ["/long-running-job"])
    fun longRunningJob(@RequestParam(defaultValue = "World") name: String): String {
        val enqueuedJobId: JobId = jobScheduler.enqueue { myService: MyService -> myService.doLongRunningJob(anArgument = "Hello $name") }
        return "Job Enqueued: $enqueuedJobId"
    }

    @GetMapping(value = ["/long-running-job-with-job-context"])
    fun longRunningJobWithJobContext(@RequestParam(defaultValue = "World") name: String): String {
        val enqueuedJobId: JobId = jobScheduler.enqueue { myService: MyService -> myService.doLongRunningJobWithJobContext(anArgument = "Hello $name", JobContext.Null) }
        return "Job Enqueued: $enqueuedJobId"
    }

    @GetMapping(value = ["/delete-job"])
    fun deleteJob(@RequestParam jobId: UUID): String {
        jobScheduler.delete(jobId)
        return "Job Deleted: $jobId"
    }
}

