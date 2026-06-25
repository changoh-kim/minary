package kr.co.data.di.service

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.service.image.ImageProcessorImpl
import kr.co.domain.service.image.ImageProcessor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ImageProcessorModule {

    @Binds
    @Singleton
    fun bindImageProcessor(impl: ImageProcessorImpl): ImageProcessor
}