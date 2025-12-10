package com.ckgod.kis.master.job

import com.ckgod.kis.master.MstFileSyncService
import kotlinx.coroutines.runBlocking
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory

class KospiSyncJob : Job {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun execute(context: JobExecutionContext) {
        val syncService = context.jobDetail.jobDataMap["syncService"] as? MstFileSyncService
            ?: run {
                logger.error("StockCodeSyncService not found in JobDataMap")
                return
            }

        logger.info("Starting scheduled KOSPI stock sync...")
        try {
            runBlocking {
                syncService.syncKospiStocks()
            }
            logger.info("KOSPI stock sync completed successfully.")
        } catch (e: Exception) {
            logger.error("Error during KOSPI stock sync", e)
        }
    }
}
