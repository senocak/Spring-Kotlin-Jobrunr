package com.github.senocak.skj

import org.jobrunr.jobs.Job
import org.jobrunr.jobs.filters.JobServerFilter
import org.slf4j.Logger
import org.springframework.stereotype.Component

@Component
class OrderFulfilmentTasksFilter: JobServerFilter {
    private val log: Logger by logger()

    override fun onFailedAfterRetries(job: Job) {
        log.warn("All retries failed for Job ${job.jobName}")
    }
}