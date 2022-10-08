@file:OptIn(ExperimentalMaterialApi::class)

package com.android.magic.ui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.magic.ui.composable.SkeletonOverlay
import com.android.magic.ui.theme.MagicTheme
import com.android.magic.utils.extractData
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

            val sheetState = rememberBottomSheetScaffoldState()

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

            BottomSheetScaffold(
                sheetContent = { BottomSheet(landmarks = viewModel.landmarks.value.extractData()) },
                modifier = Modifier.fillMaxSize(),
                scaffoldState = sheetState,
                sheetPeekHeight = 100.dp,
                sheetShape = RoundedCornerShape(24.dp)
            ) {
                Box {
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
    }
}

@Composable
fun BottomSheet(landmarks: String) {
    Column(
        modifier = Modifier
            .padding(32.dp)
            .heightIn(max = 250.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Live data",
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = landmarks,
            style = MaterialTheme.typography.body1
        )
    }
}
