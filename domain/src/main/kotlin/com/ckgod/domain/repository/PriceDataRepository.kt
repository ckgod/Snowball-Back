package com.ckgod.domain.repository

import com.ckgod.domain.model.PriceData
import java.time.LocalDate

/**
 * 가격 데이터 저장소 인터페이스
 * 백테스팅을 위한 과거 가격 데이터를 제공합니다
 */
interface PriceDataRepository {

    /**
     * 특정 기간의 가격 데이터 조회
     * @param ticker 종목 코드 (예: TQQQ, SOXL)
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 날짜순으로 정렬된 가격 데이터 리스트
     */
    suspend fun getPriceData(
        ticker: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<PriceData>

    /**
     * 특정 날짜의 가격 데이터 조회
     */
    suspend fun getPriceDataByDate(
        ticker: String,
        date: LocalDate
    ): PriceData?

    /**
     * 가격 데이터 저장 (CSV 등에서 로드 후 DB에 저장)
     */
    suspend fun savePriceData(priceData: List<PriceData>): Int
}
