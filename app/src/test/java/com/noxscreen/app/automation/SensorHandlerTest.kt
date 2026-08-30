package com.noxscreen.app.automation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SensorHandlerTest {
    private lateinit var context: Context
    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor
    private lateinit var lightSensor: Sensor
    private lateinit var accelSensor: Sensor
    private lateinit var sensorHandler: SensorHandler

    @Before
    fun setup() {
        context = mock(Context::class.java)
        sensorManager = mock(SensorManager::class.java)
        proximitySensor = mock(Sensor::class.java)
        lightSensor = mock(Sensor::class.java)
        accelSensor = mock(Sensor::class.java)

        `when`(context.getSystemService(Context.SENSOR_SERVICE)).thenReturn(sensorManager)
        `when`(sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)).thenReturn(proximitySensor)
        `when`(sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)).thenReturn(lightSensor)
        `when`(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)).thenReturn(accelSensor)
        
        `when`(proximitySensor.type).thenReturn(Sensor.TYPE_PROXIMITY)
        `when`(lightSensor.type).thenReturn(Sensor.TYPE_LIGHT)
        `when`(accelSensor.type).thenReturn(Sensor.TYPE_ACCELEROMETER)

        sensorHandler = SensorHandler(context)
    }

    @Test
    fun testStartSensors_RegistersListenersIdempotently() {
        `when`(sensorManager.registerListener(any(), eq(proximitySensor), anyInt())).thenReturn(true)
        `when`(sensorManager.registerListener(any(), eq(lightSensor), anyInt())).thenReturn(true)
        
        // Start first time
        sensorHandler.start(enableProximity = true, enableMotion = false)
        
        // Start second time
        sensorHandler.start(enableProximity = true, enableMotion = false)
        
        // Should have unregistered before registering the second time
        verify(sensorManager, times(1)).unregisterListener(sensorHandler)
        verify(sensorManager, times(2)).registerListener(any(), eq(proximitySensor), anyInt())
    }

    @Test
    fun testStopSensors_UnregistersListeners() {
        `when`(sensorManager.registerListener(any(), eq(proximitySensor), anyInt())).thenReturn(true)
        sensorHandler.start(enableProximity = true, enableMotion = false)
        sensorHandler.stop()
        
        verify(sensorManager).unregisterListener(sensorHandler)
    }

    private fun createSensorEvent(sensor: Sensor, values: FloatArray): SensorEvent {
        val constructor = SensorEvent::class.java.getDeclaredConstructor(Int::class.java)
        constructor.isAccessible = true
        val event = constructor.newInstance(values.size)
        event.sensor = sensor
        for (i in values.indices) {
            event.values[i] = values[i]
        }
        return event
    }

    @Test
    fun testOnSensorChanged_RejectsNaNValues() {
        var callbackTriggered = false
        sensorHandler.onProximityChanged = { callbackTriggered = true }
        
        `when`(sensorManager.registerListener(any(), eq(proximitySensor), anyInt())).thenReturn(true)
        sensorHandler.start(enableProximity = true, enableMotion = false)
        
        val event = createSensorEvent(proximitySensor, floatArrayOf(Float.NaN))
        sensorHandler.onSensorChanged(event)
        
        // If NaN was processed, it might trigger state changes. With validation, it should return early.
        assertFalse(callbackTriggered)
    }
}
