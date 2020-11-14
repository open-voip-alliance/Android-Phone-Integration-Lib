package nl.vialer.voip.android

import android.app.Service
import android.content.Intent
import android.os.IBinder

class VoIPService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    companion object {
        fun start() {

        }
    }
}