package com.ckgod

import com.ckgod.database.auth.AuthTokenRepository
import com.ckgod.kis.config.KisConfig
import com.ckgod.kis.config.KisMode
import com.ckgod.database.DatabaseFactory
import com.ckgod.kis.stock.repository.StockRepositoryImpl
import com.ckgod.domain.usecase.GetCurrentPriceUseCase
import com.ckgod.kis.KisApiClient
import com.ckgod.kis.auth.KisAuthService
import com.ckgod.kis.stock.api.KisStockApi
import com.ckgod.kis.master.MstFileSyncService
import com.ckgod.kis.master.MstFileDownloader
import com.ckgod.kis.master.job.KospiSyncJob
import com.ckgod.presentation.config.configureSerialization
import com.ckgod.presentation.routing.configureRouting
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    val server = embeddedServer(Netty, port = 8080, module = Application::module)

    // Subscribe to ApplicationStopping event using EmbeddedServer.monitor
    server.monitor.subscribe(ApplicationStopping) {
        QuartzSchedulerManager.shutdown()
    }

    server.start(wait = true)
}

@Suppress("unused")
fun Application.mainModule() {
    module()
}

fun Application.module() {
    // ========== Infrastructure Setup ==========
    DatabaseFactory.init()

    val config = environment.config

    val httpClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.BODY
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // ========== Configuration ==========
    val realConfig = KisConfig(
        mode = KisMode.REAL,
        baseUrl = config.property("kis.real.baseUrl").getString().trim(),
        appKey = config.property("kis.real.appKey").getString().trim(),
        appSecret = config.property("kis.real.appSecret").getString().trim(),
        accountNo = config.property("kis.real.accountNo").getString().trim(),
    )

    val mockConfig = KisConfig(
        mode = KisMode.MOCK,
        baseUrl = config.property("kis.mock.baseUrl").getString().trim(),
        appKey = config.property("kis.mock.appKey").getString().trim(),
        appSecret = config.property("kis.mock.appSecret").getString().trim(),
        accountNo = config.property("kis.mock.accountNo").getString().trim(),
    )

    // ========== Data Layer (Repositories) ==========
    val authTokenRepository = AuthTokenRepository()

    val realAuthService = KisAuthService(realConfig, httpClient, authTokenRepository)
    val mockAuthService = KisAuthService(mockConfig, httpClient, authTokenRepository)

    val realApiClient = KisApiClient(realConfig, realAuthService, httpClient)
    val mockApiClient = KisApiClient(mockConfig, mockAuthService, httpClient)

    val realStockApi = KisStockApi(realApiClient)
    val mockStockApi = KisStockApi(mockApiClient)

    val realStockRepository = StockRepositoryImpl(realStockApi)
    val mockStockRepository = StockRepositoryImpl(mockStockApi)

    // ========== Domain Layer (Use Cases) ==========
    val getCurrentPriceUseCase = GetCurrentPriceUseCase(realStockRepository)

    // ========== Background Jobs ==========
    val mstFileDownloader = MstFileDownloader(httpClient)
    val mstFileSyncService = MstFileSyncService(
        stockRepository = realStockRepository,
        downloader = mstFileDownloader,
        kospiMasterUrl = "https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip",
        outputDir = File("database/stocks")
    )

    // Initial sync if database is empty
    if (realStockRepository.isEmpty()) {
        println("KospiStocks 테이블이 비어있습니다. 초기 동기화를 시작합니다...")
        launch {
            try {
                mstFileSyncService.syncKospiStocks()
                println("초기 동기화가 완료되었습니다.")
            } catch (e: Exception) {
                println("초기 동기화 실패: ${e.message}")
                e.printStackTrace()
            }
        }
    } else {
        println("KospiStocks 테이블에 데이터가 존재합니다. 초기 동기화를 건너뜁니다.")
    }

    // Schedule periodic sync
    QuartzSchedulerManager.scheduleJob(
        jobClass = KospiSyncJob::class.java,
        jobName = "KospiSync",
        groupName = "StockSync",
        cronExpression = "0 0 7 ? * MON-FRI", // Weekdays at 07:00 AM
        jobData = mapOf("syncService" to mstFileSyncService)
    )
    QuartzSchedulerManager.start()

    // ========== Presentation Layer Setup ==========
    configureSerialization()
    configureRouting(getCurrentPriceUseCase)
}
