package com.ckgod.domain.usecase

import com.ckgod.domain.model.InvestmentStatus
import com.ckgod.domain.repository.InvestmentStatusRepository
import com.ckgod.domain.repository.StockRepository

/**
 * 주문 생성 UseCase
 *
 * 역할:
 * - ticker == null: 전체 종목 주문 생성 (Job용)
 * - ticker != null: 단일 종목 주문 생성 (API/백테스트용)
 */
class GenerateOrdersUseCase(
    private val stockRepository: StockRepository,
    private val investmentStatusRepository: InvestmentStatusRepository
) {
    /**
     * 주문 생성
     *
     * @param ticker 종목 코드 (null이면 전체 종목)
     * @return 주문 생성 결과 리스트
     */
    suspend operator fun invoke(ticker: String? = null): List<OrderResult> {
        // 대상 종목 결정
        val targets = if (ticker != null) {
            val status = investmentStatusRepository.get(ticker)
            if (status != null) listOf(status) else emptyList()
        } else {
            investmentStatusRepository.findAll()
        }

        // 각 종목 주문 생성
        return targets.mapNotNull { status ->
            try {
                generateSingle(status)
            } catch (e: Exception) {
                null // 실패한 종목은 제외
            }
        }
    }

    /**
     * 단일 종목 주문 생성
     */
    private suspend fun generateSingle(status: InvestmentStatus): OrderResult? {
        // 1. 현재 상태 조회
        val currentStatus = investmentStatusRepository.get(status.ticker) ?: return null

        // 2. 한투 API - 현재가 조회
        val marketPrice = stockRepository.getCurrentPrice(status.ticker) ?: return null

        val currentPrice = marketPrice.price.toDoubleOrNull() ?: 0.0

        /**
         * 매수 로직
         *
         * 현재 T값에 따라 매수 분배가 다름
         * 1. 전반전 (0 <= T < division / 2)
         * - 1회 매수액의 절반을 별% LOC로 매수 시도
         * - 1회 매수액의 절반을 평단가(0%) LOC로 매수 시도
         *
         * 2. 후반전 (division / 2 < T <= division -1)
         * - 1회 매수액 전체를 별% LOC로 매수 시도
         *
         * 전후반 공통으로 크게 하락하는 경우를 대비해, 1회 정액 매수를 맞추기  위해 아래로 LOC 매수를 추가 시도한다.
         * -> 폭락장이와서 1회 매수액만큼을 채우지 못했을 경우를 대비해서 어떻게든 1회 매수액을 사용하기 위함.
         * -> 주가가 폭락할 때 더 많은 수량을 구매하여 평단가를 효과적으로 낮출 수 있음
         */

        /**
         * 매도 로직
         *
         * T <= division - 1 인 경우 전후반전 상관없이 공통으로 적용
         * - 누적수량의 1/4 분량을 별% LOC 매도 시도
         * - 누적수량의 3/4 분량을 targetRate 지정가 매도 시도
         *
         * division - 1 < T < division 인 경우 쿼터 손절 기간
         * - 누적 수량의 1/4 분량을 MOC 매도로 걸어서 무조건 매도를 시도 (쿼터 손절)
         * - 누적 수량의 3/4 분량을 targetRate 지정가 매도 시도
         */

        // 3. 주문 가격 계산
        val buyPrice = currentPrice * (1.0 + currentStatus.targetRate / 100.0)
        val sellPrice = if (currentStatus.avgPrice > 0) {
            currentStatus.avgPrice * (1.0 + currentStatus.targetRate / 100.0)
        } else {
            0.0
        }

        // 4. 매수 수량 계산
        val buyQuantity = (currentStatus.oneTimeAmount / buyPrice).toInt()

        // 5. DB 업데이트 (주문 가격 저장)
        val updatedStatus = currentStatus.updateOrderPrices(currentPrice)
        investmentStatusRepository.save(updatedStatus)

        // 6. 주문 정보 반환
        return OrderResult(
            ticker = status.ticker,
            currentPrice = currentPrice,
            buyPrice = buyPrice,
            buyQuantity = buyQuantity,
            sellPrice = sellPrice,
            targetRate = currentStatus.targetRate,
            updatedStatus = updatedStatus
        )
    }

    /**
     * 주문 생성 결과
     */
    data class OrderResult(
        val ticker: String,
        val currentPrice: Double,
        val buyPrice: Double,
        val buyQuantity: Int,
        val sellPrice: Double,
        val targetRate: Double,
        val updatedStatus: InvestmentStatus
    )
}
