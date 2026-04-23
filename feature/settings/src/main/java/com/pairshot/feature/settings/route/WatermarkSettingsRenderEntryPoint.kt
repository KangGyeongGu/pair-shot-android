package com.pairshot.feature.settings.route

import com.pairshot.core.rendering.PreviewSampleProvider
import com.pairshot.core.rendering.WatermarkRenderer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WatermarkSettingsRenderEntryPoint {
    fun watermarkRenderer(): WatermarkRenderer

    fun previewSampleProvider(): PreviewSampleProvider
}
