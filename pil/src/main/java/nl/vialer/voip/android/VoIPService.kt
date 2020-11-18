package nl.vialer.voip.android

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import nl.vialer.voip.android.exception.RegistrationFailedException
import org.openvoipalliance.phonelib.model.RegistrationState


class VoIPService : Service() {

    private val voip by lazy { VoIPPIL.instance }

    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        val numberToCall = intent?.extras?.getString(EXTRA_OUTGOING_NUMBER)

        numberToCall?.let {
            voip.initialiseWithConfig()
            voip.phoneLib.register {
                if (it == RegistrationState.REGISTERED) {
                    voip.phoneLib.callTo(numberToCall)
                } else if (it == RegistrationState.FAILED) {
                    voip.phoneLib.destroy()
                    throw RegistrationFailedException()
                }
            }

        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification(): Notification {
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0, notificationIntent, 0
//        )
//
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VoIP")
            .setContentText("VoIP is running")
            .setSmallIcon(R.drawable.ic_service)
            .build()
    }

    private fun createNotificationChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "VoIP Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }


    override fun onBind(intent: Intent): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 341
        const val CHANNEL_ID = "VoIP"

        const val EXTRA_OUTGOING_NUMBER = "EXTRA_OUTGOING_NUMBER"
    }
}

fun Context.startVoipService() {
    startForegroundService(Intent(this, VoIPService::class.java))
}

fun Context.startVoipService(numberToCall: String) {
    startForegroundService(Intent(this, VoIPService::class.java).apply {
        putExtra(VoIPService.EXTRA_OUTGOING_NUMBER, numberToCall)
    })
}

fun Context.stopVoipService() {
    stopService((Intent(this, VoIPService::class.java)))
}