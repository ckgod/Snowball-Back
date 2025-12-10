package com.ckgod.presentation.dto

import com.ckgod.domain.stock.Stock
import kotlinx.serialization.Serializable

@Serializable
data class StockResponse(
    val shortCode: String,
    val standardCode: String,
    val name: String,
    val groupCode: String,
    val marketCapScale: String,
    val sectorLarge: String,
    val basePrice: Long?,
    val isTradingHalted: Boolean,
    val isManaged: Boolean,
    val marketWarning: String,
    val currentPrice: Long? = null,
    val changeRate: String? = null,
    val accumulatedVolume: Long? = null,
    val changeAmount: Long? = null,
    val changeState: String? = null
) {
    companion object {
        fun from(stock: Stock): StockResponse {
            return StockResponse(
                shortCode = stock.shortCode,
                standardCode = stock.standardCode,
                name = stock.name,
                groupCode = stock.groupCode,
                marketCapScale = stock.marketCapScale,
                sectorLarge = stock.sectorLarge,
                basePrice = stock.basePrice,
                isTradingHalted = stock.isTradingHalted,
                isManaged = stock.isManaged,
                marketWarning = stock.marketWarning,
                currentPrice = stock.currentPrice,
                changeRate = stock.changeRate,
                accumulatedVolume = stock.accumulatedVolume,
                changeAmount = stock.changeAmount,
                changeState = stock.changeState
            )
        }
    }
}
