package com.ckgod.database.trading

import com.ckgod.domain.model.PriceData
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * CSV 파일에서 가격 데이터를 로드하는 유틸리티
 *
 * CSV 형식:
 * Date,Open,High,Low,Close,Volume
 * 2023-01-03,32.50,33.75,32.25,33.50,12345678
 */
class CsvPriceDataLoader {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * CSV 파일에서 가격 데이터 로드
     * @param ticker 종목 코드
     * @param csvFile CSV 파일
     * @return 가격 데이터 리스트
     */
    fun loadFromCsv(ticker: String, csvFile: File): List<PriceData> {
        require(csvFile.exists()) { "CSV 파일이 존재하지 않습니다: ${csvFile.absolutePath}" }

        val priceDataList = mutableListOf<PriceData>()

        csvFile.bufferedReader().use { reader ->
            // 헤더 라인 스킵
            reader.readLine()

            reader.lineSequence().forEach { line ->
                if (line.isNotBlank()) {
                    try {
                        val priceData = parseCsvLine(ticker, line)
                        priceDataList.add(priceData)
                    } catch (e: Exception) {
                        System.err.println("CSV 파싱 오류: $line - ${e.message}")
                    }
                }
            }
        }

        return priceDataList.sortedBy { it.date }
    }

    /**
     * CSV 라인 파싱
     */
    private fun parseCsvLine(ticker: String, line: String): PriceData {
        val parts = line.split(",")
        require(parts.size >= 6) { "잘못된 CSV 형식: $line" }

        return PriceData(
            ticker = ticker,
            date = LocalDate.parse(parts[0].trim(), dateFormatter),
            open = parts[1].trim().toDouble(),
            high = parts[2].trim().toDouble(),
            low = parts[3].trim().toDouble(),
            close = parts[4].trim().toDouble(),
            volume = parts[5].trim().toLong()
        )
    }

    /**
     * CSV 문자열에서 가격 데이터 로드
     */
    fun loadFromCsvString(ticker: String, csvContent: String): List<PriceData> {
        val priceDataList = mutableListOf<PriceData>()
        val lines = csvContent.lines()

        // 헤더 스킵
        lines.drop(1).forEach { line ->
            if (line.isNotBlank()) {
                try {
                    val priceData = parseCsvLine(ticker, line)
                    priceDataList.add(priceData)
                } catch (e: Exception) {
                    System.err.println("CSV 파싱 오류: $line - ${e.message}")
                }
            }
        }

        return priceDataList.sortedBy { it.date }
    }
}
