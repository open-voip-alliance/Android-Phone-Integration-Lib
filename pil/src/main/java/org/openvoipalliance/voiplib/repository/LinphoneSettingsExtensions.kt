package org.openvoipalliance.voiplib.repository

import org.linphone.core.Core

/**
 * This file contains extension properties to allow for configuration of Linphone config items
 * that don't have any Java/Kotlin binding methods available, as in they are usually only set via
 * a config file.
 */
private enum class Category(val value: String) {
    SIP("sip"), AUDIO("audio"),
}

private const val AUTO_NET_STATE_MON = "auto_net_state_mon"
private const val PAUSE_CALLS_FOCUS_LOST = "android_pause_calls_when_audio_focus_lost"
private const val REGISTER_ONLY_WHEN_NETWORK_UP = "register_only_when_network_is_up"
private const val KEEPALIVE_PERIOD = "keepalive_period"
private const val AUTO_ANSWER_REPLACING_CALLS = "auto_answer_replacing_calls"
private const val PING_WITH_OPTIONS = "ping_with_options"
private const val DISABLE_AUDIO_FOCUS_REQUESTS = "android_disable_audio_focus_requests"

/**
 * Automatically handle and re-invite when network connectivity changes.
 */
var Core.automaticNetworkStateMonitoring: Boolean
    get() = config.getBool(Category.SIP.value, AUTO_NET_STATE_MON, false)
    set(value) = config.setBool(Category.SIP.value, AUTO_NET_STATE_MON, value)

/**
 * Pause calls when audio focus is lost, this should be disabled when using the Telecom Framework
 * as it can easily cause conflicts.
 */
var Core.pauseCallsWhenAudioFocusLost: Boolean
    get() = config.getBool(Category.AUDIO.value, PAUSE_CALLS_FOCUS_LOST, false)
    set(value) = config.setBool(Category.AUDIO.value, PAUSE_CALLS_FOCUS_LOST, value)

var Core.registerOnlyWhenNetworkIsUp: Boolean
    get() = config.getBool(Category.SIP.value, REGISTER_ONLY_WHEN_NETWORK_UP, false)
    set(value) = config.setBool(Category.SIP.value, REGISTER_ONLY_WHEN_NETWORK_UP, value)

var Core.keepAlivePeriod: Int
    get() = config.getInt(Category.SIP.value, KEEPALIVE_PERIOD, 0)
    set(value) = config.setInt(Category.SIP.value, KEEPALIVE_PERIOD, value)

var Core.autoAnswerReplacingCalls: Boolean
    get() = config.getBool(Category.SIP.value, AUTO_ANSWER_REPLACING_CALLS, false)
    set(value) = config.setBool(Category.SIP.value, AUTO_ANSWER_REPLACING_CALLS, value)

var Core.pingWithOptions: Boolean
    get() = config.getBool(Category.SIP.value, PING_WITH_OPTIONS, false)
    set(value) = config.setBool(Category.SIP.value, PING_WITH_OPTIONS, value)

var Core.disableAudioFocusRequests: Boolean
    get() = config.getBool(Category.AUDIO.value, DISABLE_AUDIO_FOCUS_REQUESTS, false)
    set(value) = config.setBool(Category.AUDIO.value, DISABLE_AUDIO_FOCUS_REQUESTS, value)