package com.pairshot.feature.camera.ui.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LevelSensorManager(
    context: Context,
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _roll = MutableStateFlow(0f)
    val roll: StateFlow<Float> = _roll.asStateFlow()

    private var smoothedRoll = 0f

    private val remappedMatrix = FloatArray(9)

    private val listener =
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedMatrix,
                )
                val orientation = FloatArray(3)
                SensorManager.getOrientation(remappedMatrix, orientation)
                val rawRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()

                smoothedRoll = smoothedRoll + SMOOTHING_FACTOR * (rawRoll - smoothedRoll)

                if (kotlin.math.abs(smoothedRoll - _roll.value) >= UPDATE_THRESHOLD) {
                    _roll.value = smoothedRoll
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int,
            ) {}
        }

    companion object {
        private const val SMOOTHING_FACTOR = 0.04f
        private const val UPDATE_THRESHOLD = 0.3f
    }

    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
    }
}
