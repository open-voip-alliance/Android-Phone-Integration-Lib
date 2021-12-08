package org.openvoipalliance.voiplib.repository

import android.content.Context
import android.net.ConnectivityManager

class Dns(private val context: Context) {

    /**
     * Linphone sometimes fails to resolve local dns so we will prioritise some public dns
     * and then fall-back to the local dns servers if needed (e.g. if the user blocks all remote
     * dns).
     */
    fun getServers(): Array<String> {
        val publicDnsServers = PublicDnsServer.ipAddresses
        val localDnsServers = lookUpLocalDnsServers()

        return (publicDnsServers + localDnsServers).filter { it.isNotEmpty() }.toTypedArray()
    }

    /**
     * Attempt to find the local dns, if this goes wrong we will just use an empty array.
     */
    private fun lookUpLocalDnsServers(): Array<String> =
        try {
            context.getSystemService(ConnectivityManager::class.java).run {
                getLinkProperties(activeNetwork)?.dnsServers?.map { it.hostAddress }?.toTypedArray()
            } ?: arrayOf()
        } catch (e: Throwable) {
            arrayOf()
        }

    private enum class PublicDnsServer(val ip: String) {
        GOOGLE("8.8.8.8"),
        CLOUDFLARE("1.1.1.1"),
        QUAD9("9.9.9.9");

        companion object {
            val ipAddresses: Array<String>
                get() = values().map { it.ip }.toTypedArray()
        }
    }
}

