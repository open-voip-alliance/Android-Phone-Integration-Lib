package org.openvoipalliance.androidphoneintegration.push

import android.os.Handler
import android.os.Looper
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import org.openvoipalliance.androidphoneintegration.log
import org.openvoipalliance.androidphoneintegration.logging.LogLevel

class TokenFetcher(private val middleware: Middleware?): OnCompleteListener<String> {

    private var retried = 0

    fun request() {
        if (middleware == null) {
            log("Not fetching token as middleware not present")
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(this)
    }

    override fun onComplete(task: Task<String>) {
        if (!task.isSuccessful) {
            log(
                message = "Unable to fetch FCM token, retrying soon. ${task.exception}",
                LogLevel.ERROR
            )
            retry()
            return
        }

        log("Successfully fetched FCM token, passing to client.")

        task.result?.let { token: String ->
            middleware?.tokenReceived(token)
        }
    }

    private fun retry() {
        if (retried >= MAX_RETRY_ATTEMPTS) {
            log("""
                Token fetching has failed too many times, no longer automatically retrying. 
                
                Manually call pil.token.request() to try again.
            """.trimIndent())
            return
        }

        retried++

        Handler(Looper.getMainLooper()).postDelayed({
            request()
        }, RETRY_TIME)
    }

    companion object {
        /**
         * The number of items that we will retry to fetch the token if fetching fails.
         */
        const val MAX_RETRY_ATTEMPTS = 1

        /**
         * The time between retry attempts in milliseconds.
         */
        const val RETRY_TIME = (1000 * 60).toLong()
    }
}