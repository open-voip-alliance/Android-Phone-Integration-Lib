package org.openvoipalliance.androidphoneintegration.di

import android.app.NotificationManager
import android.telecom.TelecomManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.android.PlatformIntegrator
import org.openvoipalliance.androidphoneintegration.audio.AudioManager
import org.openvoipalliance.androidphoneintegration.audio.LocalDtmfToneGenerator
import org.openvoipalliance.androidphoneintegration.call.*
import org.openvoipalliance.androidphoneintegration.call.Calls.Companion.MAX_CALLS
import org.openvoipalliance.androidphoneintegration.contacts.Contacts
import org.openvoipalliance.androidphoneintegration.events.EventsManager
import org.openvoipalliance.androidphoneintegration.helpers.VoIPLibHelper
import org.openvoipalliance.androidphoneintegration.logging.LogManager
import org.openvoipalliance.androidphoneintegration.notifications.CallNotification
import org.openvoipalliance.androidphoneintegration.notifications.IncomingCallNotification
import org.openvoipalliance.androidphoneintegration.notifications.IncomingCallRinger
import org.openvoipalliance.androidphoneintegration.telecom.AndroidCallFramework
import org.openvoipalliance.androidphoneintegration.telecom.Connection
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.repository.Dns
import org.openvoipalliance.voiplib.repository.LinphoneCoreInstanceManager
import org.openvoipalliance.voiplib.repository.call.controls.LinphoneSipActiveCallControlsRepository
import org.openvoipalliance.voiplib.repository.call.session.LinphoneSipSessionRepository
import org.openvoipalliance.voiplib.repository.initialise.LinphoneSipInitialiseRepository
import org.openvoipalliance.voiplib.repository.registration.LinphoneSipRegisterRepository

fun getModules() = listOf(pilModule)

val pilModule = module {

    single {
        AndroidCallFramework(
            androidContext(),
            get(),
            androidContext().getSystemService(TelecomManager::class.java)
        )
    }

    single { CallFactory(get()) }

    single { Contacts(androidContext()) }

    single { PlatformIntegrator(get(), get(), get()) }

    single { PIL.instance }

    single { VoIPLib.getInstance(androidContext()) }

    single { CallActions(get(), get(), get(), get()) }

    single { AudioManager(androidContext(), get(), get(), get(), get()) }

    single { EventsManager(get()) }

    single { VoIPLibHelper(get(), get(), get()) }

    factory { Connection(get(), get(), get(), get()) }

    single { Calls(get(), CallArrayList(MAX_CALLS)) }

    single { androidContext().getSystemService(NotificationManager::class.java) }

    single { androidContext().getSystemService(android.media.AudioManager::class.java) }

    single { IncomingCallNotification(get()) }

    single { IncomingCallRinger(androidContext(), get(), get()) }

    single { CallNotification() }

    single { LocalDtmfToneGenerator(get()) }

    single { LogManager(get()) }

    single { VoipLibEventTranslator(get(), get(), get()) }

    single {
        org.openvoipalliance.androidphoneintegration.notifications.NotificationManager(
            get(),
            get(),
        )
    }

    single { Dns(get()) }
    single { LinphoneCoreInstanceManager(get(), get()) }
    single { LinphoneSipInitialiseRepository(get(), get()) }
    single { LinphoneSipRegisterRepository(get(), get()) }

    single { LinphoneSipActiveCallControlsRepository(get()) }
    single { LinphoneSipSessionRepository(get(), get()) }
}