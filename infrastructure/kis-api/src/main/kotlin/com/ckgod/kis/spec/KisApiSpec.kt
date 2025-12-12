package com.ckgod.kis.spec

import com.ckgod.kis.config.KisMode
import io.ktor.http.*

sealed class KisApiSpec(
    val method: HttpMethod,
    val path: String,
    private val realTrId: String,
    private val mockTrId: String,
    val description: String
) {
    fun getTrId(mode: KisMode): String = when (mode) {
        KisMode.REAL -> realTrId
        KisMode.MOCK -> mockTrId
    }

    /**
     * 주식 현재가 조회
     * GET /uapi/domestic-stock/v1/quotations/inquire-price
     */
    data object InquirePrice : KisApiSpec(
        method = HttpMethod.Get,
        path = "/uapi/domestic-stock/v1/quotations/inquire-price",
        realTrId = "FHKST01010100",
        mockTrId = "FHKST01010100",
        description = "주식 현재가 조회"
    ) {
        fun buildQuery(
            stockCode: String,
            marketDivCode: String = "J"
        ): Map<String, String> = mapOf(
            "FID_COND_MRKT_DIV_CODE" to marketDivCode,
            "FID_INPUT_ISCD" to stockCode
        )
    }

    /**
     * @see <a href="https://apiportal.koreainvestment.com/apiservice-apiservice?/uapi/overseas-price/v1/quotations/price-detail">해외주식 현재가 상세</a>
     */
    data object QuotationPriceDetail : KisApiSpec(
        method = HttpMethod.Get,
        path = "/uapi/overseas-price/v1/quotations/price-detail",
        realTrId = "HHDFS76200200",
        mockTrId = "모의투자 미지원",
        description = "해외주식 현재가 상세"
    ) {
        fun buildQuery(
            userId: String,
            exchange: String = "NAS",
            stockCode: String
        ): Map<String, String> = mapOf(
            "AUTH" to userId,
            "EXCD" to exchange,
            "SYMB" to stockCode
        )
    }

}
