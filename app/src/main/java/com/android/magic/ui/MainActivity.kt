package com.android.magic.ui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.magic.ui.composable.SkeletonOverlay
import com.android.magic.ui.theme.MagicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MagicApp()
        }
    }
}

@Composable
fun MagicApp() {
    MagicTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            val context = LocalContext.current

            val viewModel: MediaPipeViewModel = hiltViewModel()

            val previewView = remember {
                FrameLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }

            LaunchedEffect("") {
                viewModel.setup(context, previewView)
            }

            AndroidView(
                modifier = Modifier,
                factory = {
                    previewView
                }
            )

            viewModel.landmarks.value?.let {
                SkeletonOverlay(landmarks = it)
            }
        }
    }
}
