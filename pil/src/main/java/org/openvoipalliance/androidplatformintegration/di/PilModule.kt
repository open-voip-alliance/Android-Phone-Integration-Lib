package org.openvoipalliance.androidplatformintegration.di

import android.telecom.TelecomManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.openvoipalliance.androidplatformintegration.call.CallManager
import org.openvoipalliance.androidplatformintegration.call.Calls
import org.openvoipalliance.androidplatformintegration.PIL
import org.openvoipalliance.androidplatformintegration.helpers.PhoneLibHelper
import org.openvoipalliance.androidplatformintegration.audio.AudioManager
import org.openvoipalliance.androidplatformintegration.call.CallActions
import org.openvoipalliance.androidplatformintegration.call.PILCallFactory
import org.openvoipalliance.androidplatformintegration.contacts.Contacts
import org.openvoipalliance.androidplatformintegration.events.EventsManager
import org.openvoipalliance.androidplatformintegration.telecom.AndroidCallFramework
import org.openvoipalliance.androidplatformintegration.telecom.Connection
import org.openvoipalliance.phonelib.PhoneLib

fun getModules() = listOf(pilModule)

val pilModule = module {

    single {
        AndroidCallFramework(
            androidContext(),
            androidContext().getSystemService(TelecomManager::class.java)
        )
    }

    single { PILCallFactory(get(), get()) }

    single { Contacts(androidContext()) }

    single { CallManager(get(), get()) }

    single { PIL.instance }

    single { PhoneLib.getInstance(androidContext()) }

    single { CallActions(get(), get(), get(), get()) }

    single { AudioManager(get(), get()) }

    single { EventsManager(get()) }

    single { PhoneLibHelper(get(), get(), get()) }

    factory { Connection(get(), get(), get(), get()) }

    factory { Calls(get(), get()) }
}