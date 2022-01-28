package org.openvoipalliance.androidphoneintegration.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.dsl.koinApplication

internal lateinit var di: KoinApplication

internal fun initPilKoin(context: Context) {
    di = koinApplication {
        androidContext(context)
        modules(getModules())
    }
}

@KoinApiExtension
internal interface PilKoinComponent : KoinComponent {
    override fun getKoin(): Koin {
        return di.koin
    }
}