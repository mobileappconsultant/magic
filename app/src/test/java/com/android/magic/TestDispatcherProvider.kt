package com.android.magic

import com.android.magic.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class TestDispatcherProvider : DispatcherProvider {
    override val io: CoroutineDispatcher
        get() = UnconfinedTestDispatcher()
}
