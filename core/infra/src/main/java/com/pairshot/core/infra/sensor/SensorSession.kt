package com.pairshot.core.infra.sensor

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.StateFlow

interface SensorSession {
    val roll: StateFlow<Float>
    val deviceOrientation: StateFlow<Int>

    fun bind(owner: LifecycleOwner)

    fun release()
}
