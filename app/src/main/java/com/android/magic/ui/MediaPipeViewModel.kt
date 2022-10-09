package com.android.magic.ui

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.magic.mediapipe.MediaPipe
import com.google.mediapipe.formats.proto.LandmarkProto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class MediaPipeViewModel @Inject constructor(
    private val poseTracking: MediaPipe,
) : ViewModel() {

    private val _landmarks: MutableStateFlow<List<LandmarkProto.NormalizedLandmark>?> = MutableStateFlow(null)

    val landmarks: StateFlow<List<LandmarkProto.NormalizedLandmark>?> = _landmarks.asStateFlow()

    fun setup(context: Context, view: ViewGroup) {
        viewModelScope.launch {
            loadPoseEstimation(context, view)
        }
    }
    private fun loadPoseEstimation(context: Context, view: ViewGroup) = viewModelScope.launch {
        poseTracking.addLandmarkCallback {
            it.landmarkList?.let { landmarkList ->
                _landmarks.value = landmarkList
            }
        }

        poseTracking.init(view, context)
        poseTracking.startCamera(context)
    }

    public override fun onCleared() {
        Timber.d("onCleared")
        poseTracking.onClose()
        super.onCleared()
    }
}
