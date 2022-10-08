package com.android.magic.ui

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.magic.mediapipe.MediaPipe
import com.google.mediapipe.formats.proto.LandmarkProto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MediaPipeViewModel @Inject constructor(
    private val poseTracking: MediaPipe,
) : ViewModel() {

    var landmarks: MutableState<List<LandmarkProto.NormalizedLandmark>?> = mutableStateOf(null)

    fun setup(context: Context, view: ViewGroup) {
        viewModelScope.launch {
            loadPoseEstimation(context, view)
        }
    }
    private fun loadPoseEstimation(context: Context, view: ViewGroup) {
        poseTracking.addLandmarkCallback {
            it.landmarkList?.let { landmarkList ->
                landmarks.value = landmarkList
                Timber.v(landmarks.toString())
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
