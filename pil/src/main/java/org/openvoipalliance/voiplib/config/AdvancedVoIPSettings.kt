package org.openvoipalliance.voiplib.config

data class AdvancedVoIPSettings(
        val echoCancellation: Boolean = true,
        val adaptiveRateControl: Boolean = true,
        val jitterCompensation: Boolean = true,
        val mtu: Int = 1300,
        val mediaMultiplexing: Boolean = false,
        val adaptiveRateAlgorithm: AdaptiveRateAlgorithm = AdaptiveRateAlgorithm.ADVANCED
)

enum class AdaptiveRateAlgorithm {
    BASIC, ADVANCED
}