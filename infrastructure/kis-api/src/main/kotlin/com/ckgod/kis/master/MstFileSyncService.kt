package com.ckgod.kis.master

import com.ckgod.domain.stock.StockRepository
import java.io.File

class MstFileSyncService(
    private val stockRepository: StockRepository,
    private val downloader: MstFileDownloader,
    private val kospiMasterUrl: String,
    private val outputDir: File
) {
    suspend fun syncKospiStocks() {
        val mstFile = downloader.downloadAndUnzip(kospiMasterUrl, outputDir)
        val stocks = MstFileParser.parse(mstFile)

        stockRepository.deleteAll()
        stockRepository.saveAll(stocks)

        println("KOSPI 종목 ${stocks.size}개 동기화 완료")
    }
}