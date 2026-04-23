package com.pairshot.core.infra.di

import com.pairshot.core.infra.camera.CameraSession
import com.pairshot.core.infra.camera.CameraSessionImpl
import com.pairshot.core.infra.sensor.SensorSession
import com.pairshot.core.infra.sensor.SensorSessionImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class InfraModule {
    @Binds
    @ViewModelScoped
    abstract fun bindCameraSession(impl: CameraSessionImpl): CameraSession

    @Binds
    @ViewModelScoped
    abstract fun bindSensorSession(impl: SensorSessionImpl): SensorSession
}
