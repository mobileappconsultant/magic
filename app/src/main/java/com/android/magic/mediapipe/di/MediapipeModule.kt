package com.android.magic.mediapipe.di

import com.android.magic.mediapipe.MediaPipe
import com.android.magic.mediapipe.PoseEstimation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MediapipeModule {

    @Binds
    abstract fun bindMediaPipe(poseEstimation: PoseEstimation): MediaPipe
}
