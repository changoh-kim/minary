package kr.co.data.di.module.util

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.feature.profile.service.ImageProcessorImpl
import kr.co.domain.feature.profile.service.ImageProcessor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ImageProcessorModule {

    @Binds
    @Singleton
    fun bindImageProcessor(impl: ImageProcessorImpl): ImageProcessor
}