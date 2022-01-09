package org.openvoipalliance.androidphoneintegration.example

import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.openvoipalliance.androidphoneintegration.push.Middleware
import org.openvoipalliance.androidphoneintegration.push.UnavailableReason
import java.io.IOException

class VoIPGRIDMiddleware(private val context: Context) : Middleware {

    private val prefs by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private val client = OkHttpClient()
    suspend fun register(): Boolean = withContext(Dispatchers.IO) {
        val data = FormBody.Builder().apply {
            add("name", prefs.getString("voipgrid_username", "")!!)
            add("token", token!!)
            add("sip_user_id", prefs.getString("username", "")!!)
            add("os_version", Build.VERSION.CODENAME)
            add("client_version", Build.VERSION.RELEASE)
            add("app", context.packageName!!)
        }.build()

        val request = createMiddlewareRequest()
            .post(data)
            .build()

        return@withContext client.newCall(request).execute().isSuccessful
    }

    suspend fun unregister(): Boolean = withContext(Dispatchers.IO) {
        val data = FormBody.Builder().apply {
            add("token", token!!)
            add("sip_user_id", prefs.getString("username", "")!!)
            add("app", context.packageName!!)
        }.build()

        val request = createMiddlewareRequest()
            .delete(data)
            .build()

        return@withContext client.newCall(request).execute().isSuccessful
    }

    private fun createMiddlewareRequest(url: String = "https://vialerpush.voipgrid.nl/api/android-device/") = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Token ${prefs.getString("voipgrid_username", "")}:${prefs.getString("voipgrid_api_token", "")}")

    override fun respond(
        remoteMessage: RemoteMessage,
        available: Boolean,
        reason: UnavailableReason?,
    ) {
        val data = FormBody.Builder().apply {
            add("unique_key", remoteMessage.data["unique_key"]!!)
            add("available", if (available) "true" else "false")
            add("message_start_time", remoteMessage.data["message_start_time"]!!)
            add("sip_user_id", prefs.getString("username", "")!!)
        }.build()

        val request = createMiddlewareRequest("https://vialerpush.voipgrid.nl/api/call-response/")
            .post(data)
            .build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                }
            }
        )
    }

    override fun tokenReceived(token: String) {
        Companion.token = token
    }

    companion object {
        var token: String? = null
    }
}
