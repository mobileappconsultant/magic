plugins {
    id(Plugins.androidApplication)
    kotlin(Plugins.android)
    kotlin(Plugins.kapt)
    id(Plugins.parcelize)
    id(Plugins.hilt)
    id(Plugins.jacoco)
}

android {
    compileSdk = 33

    lint {
        abortOnError = false
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    defaultConfig {
        applicationId = "com.android.magic"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.android.magic.HiltTestRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Compose.composeVersion
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    implementation(Kotlin.core)
    implementation(Compose.ui)
    implementation(Compose.hilt)
    implementation(Compose.uiTooling)
    implementation(Compose.livedata)
    implementation(Compose.foundation)
    implementation(Compose.material)
    implementation(Compose.materialIconsCore)
    implementation(Compose.materialIconExtended)
    implementation(Compose.accompanistPermission)
    implementation("androidx.lifecycle:lifecycle-process:2.4.1")
    androidTestImplementation(Compose.composeUiTest)
    implementation(Compose.navigation)

    implementation(Timber.library)
    implementation(Navigation.navigation)
    implementation(Navigation.navigationUI)

    implementation(Kotlin.coroutines)
    implementation(Kotlin.coroutinesCore)
    implementation(Kotlin.coroutineReactive)

    testImplementation(Tests.coreTesting)
    testImplementation(Tests.core)
    testImplementation(Tests.coroutineTest)
    testImplementation(Tests.robolectric)
    testImplementation(Tests.mockk)
    testImplementation(Tests.extJUnit)
    testImplementation(Tests.espressoCore)
    testImplementation(Tests.junit)
    testImplementation(Tests.composeUiTest)
    testImplementation(Tests.hiltTesting)
    testImplementation(Tests.mockWebServer)
    testImplementation(Tests.idling)
    kaptTest(Tests.hiltCompiler)
    debugImplementation(Tests.composeDebugTest)

    // Hilt
    implementation(Hilt.hiltAndroid)
    kapt(Hilt.hiltAndroidCompiler)
    kapt(Hilt.hiltCompiler)
    implementation(Hilt.hiltNavigation)

    implementation(Camera.camera)
    implementation(Camera.cameraLifecycle)
    implementation(Camera.cameraView)

    implementation("com.google.flogger:flogger:0.3.1")
    implementation("com.google.flogger:flogger-system-backend:0.3.1")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.google.guava:guava:27.0.1-android")
    implementation("com.google.guava:guava:27.0.1-android")
    implementation("com.google.protobuf:protobuf-java:3.11.4")
}

jacoco {
    toolVersion = Build.jacocoVersion
}
