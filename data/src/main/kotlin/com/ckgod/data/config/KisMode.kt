package com.ckgod.data.config

enum class KisMode {
    REAL,
    MOCK;

    companion object {
        fun from(value: String?): KisMode {
            return when (value?.uppercase()) {
                "REAL" -> REAL
                else -> MOCK
            }
        }
    }
}
