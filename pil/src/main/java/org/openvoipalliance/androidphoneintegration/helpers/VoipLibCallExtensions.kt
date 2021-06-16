package org.openvoipalliance.androidphoneintegration.helpers

import org.openvoipalliance.voiplib.model.Call

internal val Call.identifier: String
    get() = linphoneCall.hashCode().toString()