package com.noxscreen.app.automation

import kotlin.math.abs

object PocketConstants {
    const val POCKET_ENTER_THRESHOLD = 75
    const val POCKET_EXIT_THRESHOLD = 35
    const val POCKET_ENTER_DEBOUNCE_MS = 400L
    const val POCKET_EXIT_DEBOUNCE_MS = 200L
    const val LOW_LIGHT_THRESHOLD = 2.0f
    const val DARK_LIGHT_THRESHOLD = 15.0f
}

enum class PocketState {
    OUT_OF_POCKET,
    DETECTING_POCKET,
    IN_POCKET,
    DETECTING_REMOVAL
}

object PocketDecisionEngine {

    fun calculateConfidence(
        isProximityNear: Boolean,
        hasLightSensor: Boolean,
        lux: Float,
        hasGravity: Boolean,
        gx: Float,
        gy: Float,
        gz: Float
    ): Int {
        if (!isProximityNear) return 0

        var confidence = 50

        // Light sensor analysis
        if (hasLightSensor) {
            if (lux < PocketConstants.LOW_LIGHT_THRESHOLD) {
                confidence += 30
            } else if (lux < PocketConstants.DARK_LIGHT_THRESHOLD) {
                confidence += 15
            }
        } else {
            // Graceful fallback for devices without ambient light sensors
            confidence += 15
        }

        // Gravity & orientation analysis
        if (hasGravity) {
            val isFlatFaceUp = gz > 7.5f && abs(gx) < 4.0f && abs(gy) < 4.0f
            val isFaceDown = gz < -7.0f && abs(gx) < 4.0f && abs(gy) < 4.0f
            val isVerticalOrTilted = abs(gy) > 5.0f || (gz in -7.0f..6.0f && (abs(gx) > 3.0f || abs(gy) > 3.0f))

            if (isFlatFaceUp) {
                confidence -= 50
            } else if (isVerticalOrTilted || isFaceDown) {
                confidence += 30
            }
        } else {
            // Graceful fallback for devices without accelerometer/gravity
            confidence += 20
        }

        return confidence.coerceIn(0, 100)
    }

    fun shouldEnterPocket(confidence: Int): Boolean {
        return confidence >= PocketConstants.POCKET_ENTER_THRESHOLD
    }

    fun shouldExitPocket(confidence: Int): Boolean {
        return confidence <= PocketConstants.POCKET_EXIT_THRESHOLD
    }
}
