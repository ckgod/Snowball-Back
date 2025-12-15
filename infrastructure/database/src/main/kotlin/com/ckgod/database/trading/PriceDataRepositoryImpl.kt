package com.ckgod.database.trading

import com.ckgod.domain.model.PriceData
import com.ckgod.domain.repository.PriceDataRepository
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.javatime.date
import java.time.LocalDate

/**
 * 가격 데이터 테이블
 */
object PriceDataTable : Table("price_data") {
    val ticker = varchar("ticker", 20)
    val date = date("date")
    val open = double("open")
    val high = double("high")
    val low = double("low")
    val close = double("close")
    val volume = long("volume")

    override val primaryKey = PrimaryKey(arrayOf(ticker, date))
}

/**
 * 가격 데이터 저장소 구현체
 */
class PriceDataRepositoryImpl : PriceDataRepository {

    init {
        transaction {
            SchemaUtils.create(PriceDataTable)
        }
    }

    override suspend fun getPriceData(
        ticker: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<PriceData> = transaction {
        PriceDataTable
            .selectAll()
            .where {
                (PriceDataTable.ticker eq ticker) and
                        (PriceDataTable.date greaterEq startDate) and
                        (PriceDataTable.date lessEq endDate)
            }
            .orderBy(PriceDataTable.date)
            .map { it.toPriceData() }
    }

    override suspend fun getPriceDataByDate(
        ticker: String,
        date: LocalDate
    ): PriceData? = transaction {
        PriceDataTable
            .selectAll()
            .where {
                (PriceDataTable.ticker eq ticker) and
                        (PriceDataTable.date eq date)
            }
            .singleOrNull()
            ?.toPriceData()
    }

    override suspend fun savePriceData(priceData: List<PriceData>): Int = transaction {
        var count = 0
        priceData.forEach { data ->
            val existing = PriceDataTable.selectAll()
                .where {
                    (PriceDataTable.ticker eq data.ticker) and
                            (PriceDataTable.date eq data.date)
                }
                .singleOrNull()

            if (existing == null) {
                PriceDataTable.insert {
                    it[ticker] = data.ticker
                    it[date] = data.date
                    it[open] = data.open
                    it[high] = data.high
                    it[low] = data.low
                    it[close] = data.close
                    it[volume] = data.volume
                }
                count++
            }
        }
        count
    }

    private fun ResultRow.toPriceData() = PriceData(
        ticker = this[PriceDataTable.ticker],
        date = this[PriceDataTable.date],
        open = this[PriceDataTable.open],
        high = this[PriceDataTable.high],
        low = this[PriceDataTable.low],
        close = this[PriceDataTable.close],
        volume = this[PriceDataTable.volume]
    )
}
