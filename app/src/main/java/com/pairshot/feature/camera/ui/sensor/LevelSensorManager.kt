package com.pairshot.feature.camera.ui.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
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

    // start()/stop() 호출 기반 논리 상태. level 기능의 on/off 의도를 추적한다.
    private var isRunning = false

    // ON_STOP 진입 시점에 isRunning 이었는지 기억해 ON_START 시 복원에 사용한다.
    private var wasActiveBeforeStop = false

    private var lifecycleObserver: LifecycleEventObserver? = null
    private var observedLifecycle: Lifecycle? = null

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

    /**
     * Lifecycle을 관찰하여 ON_STOP 시 센서를 자동으로 정지하고,
     * ON_START 시 정지 전에 활성 상태였다면 센서를 재시작한다.
     *
     * - level 기능이 꺼져 있는 상태(isRunning == false)에서 백그라운드 진입 후 복귀해도
     *   센서를 켜지 않는다.
     * - CameraScreen / AfterCameraScreen에서 LocalLifecycleOwner.current.lifecycle을 전달한다.
     * - [stop] 호출 시 observer도 함께 정리된다.
     */
    fun observeLifecycle(lifecycle: Lifecycle) {
        if (observedLifecycle === lifecycle) return

        // 이전 observer 정리
        lifecycleObserver?.let { observedLifecycle?.removeObserver(it) }

        val observer =
            LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> {
                        wasActiveBeforeStop = isRunning
                        if (isRunning) {
                            sensorManager.unregisterListener(listener)
                            // isRunning 논리 상태는 유지 — level 기능 자체는 아직 on
                        }
                    }

                    Lifecycle.Event.ON_START -> {
                        if (wasActiveBeforeStop) {
                            rotationSensor?.let {
                                sensorManager.registerListener(
                                    listener,
                                    it,
                                    SensorManager.SENSOR_DELAY_UI,
                                )
                            }
                            wasActiveBeforeStop = false
                        }
                    }

                    else -> {
                        Unit
                    }
                }
            }

        lifecycleObserver = observer
        observedLifecycle = lifecycle
        lifecycle.addObserver(observer)
    }

    /** level 기능을 활성화하고 센서 수신을 시작한다. */
    fun start() {
        isRunning = true
        rotationSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * level 기능을 비활성화하고 센서 수신을 정지한다.
     * ViewModel의 onCleared() 혹은 toggleLevel() off 경로에서 호출된다.
     * lifecycle observer도 함께 정리한다.
     */
    fun stop() {
        isRunning = false
        wasActiveBeforeStop = false
        sensorManager.unregisterListener(listener)

        lifecycleObserver?.let { observedLifecycle?.removeObserver(it) }
        lifecycleObserver = null
        observedLifecycle = null
    }
}
