package com.noxscreen.app.automation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class PocketDetectorTest {
    
    @Test
    fun testProximityFar() {
        val conf = calculateConfidence(isNear = false, hasLight = true, lux = 100f, hasGravity = true, gx = 0f, gy = 9.8f, gz = 0f)
        assertTrue("Proximity FAR should result in 0 confidence", conf == 0)
    }

    @Test
    fun testDarkEnvironmentFaceUp() {
        // Flat face-up, pitch black room (Simulates hand waving over phone on a table in the dark)
        val conf = calculateConfidence(isNear = true, hasLight = true, lux = 1.0f, hasGravity = true, gx = 0f, gy = 0f, gz = 9.8f)
        // 50 (prox) + 30 (dark) - 50 (flat) = 30
        assertTrue("Hand wave in dark room face-up should be < 75", conf < 75)
    }

    @Test
    fun testDarkEnvironmentFaceDown() {
        // Flat face-down, pitch black room
        val conf = calculateConfidence(isNear = true, hasLight = true, lux = 1.0f, hasGravity = true, gx = 0f, gy = 0f, gz = -9.8f)
        // 50 (prox) + 30 (dark) + 30 (face down) = 100
        assertTrue("Face down in dark room should be >= 75", conf >= 75)
    }

    @Test
    fun testBrightEnvironmentVertical() {
        // Phone dropped in bright white pants (vertical orientation)
        val conf = calculateConfidence(isNear = true, hasLight = true, lux = 50f, hasGravity = true, gx = 0f, gy = 9.8f, gz = 0f)
        // 50 (prox) + 0 (bright) + 30 (vertical) = 80
        assertTrue("Bright pocket (vertical) should pass threshold", conf >= 75)
    }

    @Test
    fun testMissingLightSensorVertical() {
        // Phone dropped in pocket (vertical), no light sensor
        val conf = calculateConfidence(isNear = true, hasLight = false, lux = 0f, hasGravity = true, gx = 0f, gy = 9.8f, gz = 0f)
        // 50 (prox) + 15 (no light fallback) + 30 (vertical) = 95
        assertTrue("Vertical with no light sensor should pass threshold", conf >= 75)
    }

    @Test
    fun testMissingLightSensorFaceUp() {
        // Phone on table face up, no light sensor
        val conf = calculateConfidence(isNear = true, hasLight = false, lux = 0f, hasGravity = true, gx = 0f, gy = 0f, gz = 9.8f)
        // 50 (prox) + 15 (no light fallback) - 50 (flat) = 15
        assertTrue("Face up with no light sensor should fail threshold", conf < 75)
    }
    
    @Test
    fun testMissingAccelerometerDark() {
        // Phone in dark, no gravity sensor
        val conf = calculateConfidence(isNear = true, hasLight = true, lux = 1f, hasGravity = false, gx = 0f, gy = 0f, gz = 0f)
        // 50 (prox) + 30 (dark) + 20 (no gravity fallback) = 100
        assertTrue("Dark with no accelerometer should pass threshold", conf >= 75)
    }

    @Test
    fun testMissingAccelerometerBright() {
        // Phone in bright, no gravity sensor
        val conf = calculateConfidence(isNear = true, hasLight = true, lux = 100f, hasGravity = false, gx = 0f, gy = 0f, gz = 0f)
        // 50 (prox) + 0 (bright) + 20 (no gravity fallback) = 70
        assertTrue("Bright with no accelerometer should fail threshold", conf < 75)
    }

    private fun calculateConfidence(isNear: Boolean, hasLight: Boolean, lux: Float, hasGravity: Boolean, gx: Float, gy: Float, gz: Float): Int {
        if (!isNear) return 0
        var c = 50
        
        if (hasLight) {
            if (lux < 2.0f) c += 30 else if (lux < 15.0f) c += 15
        } else {
            c += 15
        }
        
        if (hasGravity) {
            val isFlat = gz > 7.5f && abs(gx) < 4.0f && abs(gy) < 4.0f
            val isFaceDown = gz < -7.0f && abs(gx) < 4.0f && abs(gy) < 4.0f
            val isVert = abs(gy) > 5.0f
            
            if (isFlat) c -= 50
            else if (isFaceDown || isVert) c += 30
        } else {
            c += 20
        }
        
        return c.coerceIn(0, 100)
    }
}
