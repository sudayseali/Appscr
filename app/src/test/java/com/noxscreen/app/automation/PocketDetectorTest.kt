package com.noxscreen.app.automation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PocketDetectorTest {
    
    @Test
    fun testProximityFarProducesZeroConfidence() {
        val conf = PocketDecisionEngine.calculateConfidence(
            isProximityNear = false,
            hasLightSensor = true,
            lux = 100f,
            hasGravity = true,
            gx = 0f,
            gy = 9.8f,
            gz = 0f
        )
        assertEquals(0, conf)
        assertFalse(PocketDecisionEngine.shouldEnterPocket(conf))
        assertTrue(PocketDecisionEngine.shouldExitPocket(conf))
    }

    @Test
    fun testDarkEnvironmentFaceUpRejection() {
        // Flat face-up, pitch black room (Simulates hand waving over phone on a table in the dark)
        val conf = PocketDecisionEngine.calculateConfidence(
            isProximityNear = true,
            hasLightSensor = true,
            lux = 1.0f,
            hasGravity = true,
            gx = 0f,
            gy = 0f,
            gz = 9.8f
        )
        // 50 (prox) + 30 (dark) - 50 (flat face up) = 30
        assertTrue("Hand wave in dark room face-up must be < 75", conf < 75)
        assertFalse(PocketDecisionEngine.shouldEnterPocket(conf))
    }

    @Test
    fun testDarkEnvironmentFaceDown() {
        // Flat face-down, pitch black room
        val conf = PocketDecisionEngine.calculateConfidence(
            isProximityNear = true,
            hasLightSensor = true,
            lux = 1.0f,
            hasGravity = true,
            gx = 0f,
            gy = 0f,
            gz = -9.8f
        )
        // 50 (prox) + 30 (dark) + 30 (face down) = 100 -> capped at 100
        assertTrue("Face down in dark room should be >= 75", conf >= 75)
        assertTrue(PocketDecisionEngine.shouldEnterPocket(conf))
    }

    @Test
    fun testBrightEnvironmentVerticalPocket() {
        // Phone dropped in bright white pants / thin pocket (vertical orientation, light leakage)
        val conf = PocketDecisionEngine.calculateConfidence(
            isProximityNear = true,
            hasLightSensor = true,
            lux = 50f,
            hasGravity = true,
            gx = 0f,
            gy = 9.8f,
            gz = 0f
        )
        // 50 (prox) + 0 (bright) + 30 (vertical) = 80
        assertTrue("Bright pocket (vertical) should pass threshold", conf >= 75)
        assertTrue(PocketDecisionEngine.shouldEnterPocket(conf))
    }

    @Test
    fun testDimEnvironmentTiltedPocket() {
        // Phone sitting in pocket at a 45 degree tilt
        val conf = PocketDecisionEngine.calculateConfidence(
            isProximityNear = true,
            hasLightSensor = true,
            lux = 8.0f,
            hasGravity = true,
            gx = 4.0f,
            gy = 6.0f,
            gz = 3.0f
        )
        // 50 (prox) + 15 (dim) + 30 (tilted) = 95
        assertTrue("Tilted pocket in dim light should pass threshold", conf >= 75)
        assertTrue(PocketDecisionEngine.shouldEnterPocket(conf))
    }

    @Test
    fun testMissingLightSensorVertical() {
        // Phone dropped in pocket (vertical), no light sensor available on hardware
        val conf = PocketDecisionEngine.calculateConfidence(
            isProximityNear = true,
            hasLightSensor = false,
            lux = 0f,
            hasGravity = true,
            gx = 0f,
            gy = 9.8f,
            gz = 0f
        )
        // 50 (prox) + 15 (fallback) + 30 (vertical) = 95
        assertTrue("Vertical with no light sensor should pass threshold", conf >= 75)
        assertTrue(PocketDecisionEngine.shouldEnterPocket(conf))
    }

    @Test
    fun testMissingLightSensorFaceUp() {
        // Phone on table face up, no light sensor
        val conf = PocketDecisionEngine.calculateConfidence(
            isProximityNear = true,
            hasLightSensor = false,
            lux = 0f,
            hasGravity = true,
            gx = 0f,
            gy = 0f,
            gz = 9.8f
        )
        // 50 (prox) + 15 (fallback) - 50 (flat face up) = 15
        assertTrue("Face up with no light sensor should fail threshold", conf < 75)
        assertFalse(PocketDecisionEngine.shouldEnterPocket(conf))
    }
    
    @Test
    fun testMissingAccelerometerDark() {
        // Phone in dark, no gravity sensor
        val conf = PocketDecisionEngine.calculateConfidence(
            isProximityNear = true,
            hasLightSensor = true,
            lux = 1f,
            hasGravity = false,
            gx = 0f,
            gy = 0f,
            gz = 0f
        )
        // 50 (prox) + 30 (dark) + 20 (fallback) = 100
        assertTrue("Dark with no accelerometer should pass threshold", conf >= 75)
        assertTrue(PocketDecisionEngine.shouldEnterPocket(conf))
    }

    @Test
    fun testMissingAccelerometerBright() {
        // Phone in bright, no gravity sensor
        val conf = PocketDecisionEngine.calculateConfidence(
            isProximityNear = true,
            hasLightSensor = true,
            lux = 100f,
            hasGravity = false,
            gx = 0f,
            gy = 0f,
            gz = 0f
        )
        // 50 (prox) + 0 (bright) + 20 (fallback) = 70
        assertTrue("Bright with no accelerometer should fail threshold", conf < 75)
        assertFalse(PocketDecisionEngine.shouldEnterPocket(conf))
    }

    @Test
    fun testHysteresisThresholds() {
        // Verify hysteresis gap: Enter (>=75) is strictly greater than Exit (<=35)
        assertTrue(PocketConstants.POCKET_ENTER_THRESHOLD > PocketConstants.POCKET_EXIT_THRESHOLD)
        assertEquals(75, PocketConstants.POCKET_ENTER_THRESHOLD)
        assertEquals(35, PocketConstants.POCKET_EXIT_THRESHOLD)
        
        // Intermediate confidence (e.g. 50) should neither enter nor exit
        assertFalse(PocketDecisionEngine.shouldEnterPocket(50))
        assertFalse(PocketDecisionEngine.shouldExitPocket(50))
    }
}
