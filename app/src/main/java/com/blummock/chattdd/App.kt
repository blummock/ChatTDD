package com.blummock.chattdd

import android.app.Application
import com.blummock.chattdd.chat_feature.data.UseCasesProvider

class App : Application() {

    val useCasesProvider by lazy(LazyThreadSafetyMode.NONE) { UseCasesProvider(this) }
}