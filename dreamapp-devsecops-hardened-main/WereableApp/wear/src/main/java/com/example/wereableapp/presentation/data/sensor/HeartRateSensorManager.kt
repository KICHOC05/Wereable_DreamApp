package com.example.wereableapp.presentation.data.sensor
/*
* El codigo implementa un SensorManager para el sensor de frecuencia cardíaca.
* */

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class HeartRateSensorManager(
    private val context: Context,
    private val onHeartRateChanged: (Float?) -> Unit
) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val heartRateSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null && event.values.isNotEmpty()) {
                val bpm = event.values[0]
                onHeartRateChanged(bpm)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Se puede usar para validar precisión si se requiere.
        }
    }

    /**
     * Inicia la lectura del sensor.
     */
    fun start() {
        if (heartRateSensor == null) {
            Log.e("HeartRateSensorManager", "❌ Sensor de frecuencia cardíaca no disponible.")
            onHeartRateChanged(null)
            return
        }

        sensorManager.registerListener(
            listener,
            heartRateSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    /**
     * Detiene la lectura del seonsor.
     */
    fun stop() {
        sensorManager.unregisterListener(listener)
    }
}
