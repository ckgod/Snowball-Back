package com.ckgod.domain.model

data class StockPrice(
    val code: String,
    val currentPrice: String,
    val changeRate: String,
    val volume: String
)
