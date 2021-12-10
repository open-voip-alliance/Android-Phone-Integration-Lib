package org.openvoipalliance.androidphoneintegration.push

import com.google.firebase.messaging.RemoteMessage

interface Middleware {

    fun respond(remoteMessage: RemoteMessage, available: Boolean)

    fun tokenReceived(token: String)

    /**
     * Inspect the contents of the push notification to determine whether the
     * contents is a push message.
     *
     * @return If TRUE Is returned, processing of the push message will continue as if it
     * is a call. If FALSE is returned, nothing further will be done with this notification.
     */
    fun inspect(remoteMessage: RemoteMessage): Boolean = true
}
