import com.ckgod.database.trading.CsvPriceDataLoader
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

/**
 * 가격 데이터 로더 테스트
 */
class PriceDataRepositoryTest {

    @Test
    fun testCsvLoader() = runBlocking<Unit> {
        val csvLoader = CsvPriceDataLoader()
        val sampleCsv = """
Date,Open,High,Low,Close,Volume
2024-01-02,50.00,52.00,49.50,51.50,12345678
2024-01-03,51.60,53.20,51.00,52.80,13456789
2024-01-04,52.90,54.50,52.50,54.00,14567890
        """.trimIndent()

        val priceData = csvLoader.loadFromCsvString("TQQQ", sampleCsv)

        assertTrue(priceData.isNotEmpty(), "가격 데이터가 로드되어야 합니다")
        assertTrue(priceData.size == 3, "3일의 데이터가 있어야 합니다")
        assertTrue(priceData[0].ticker == "TQQQ", "티커가 TQQQ여야 합니다")
        assertTrue(priceData[0].close == 51.50, "종가가 정확해야 합니다")
    }
}
