package org.openvoipalliance.androidphoneintegration.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME
import android.provider.ContactsContract.PhoneLookup.PHOTO_URI
import androidx.core.content.ContextCompat
import org.openvoipalliance.androidphoneintegration.di.CurrentPreferencesResolver
import org.openvoipalliance.androidphoneintegration.helpers.identifier
import org.openvoipalliance.voiplib.model.Call

internal class Contacts(private val context: Context, private val preferences: CurrentPreferencesResolver) {

    /**
     * We don't want to query the contacts multiple times per call as we may be creating the
     * call object frequently so we will cache any results against the call id.
     */
    private val cachedContacts = mutableMapOf<String, Contact?>()

    fun find(call: Call): Contact? {
        if (cachedContacts.containsKey(call.identifier)) return cachedContacts[call.identifier]

        return find(call.phoneNumber).also {
            cachedContacts[call.identifier] = it
        }
    }

    private fun find(number: String): Contact? {
        val contact = if (hasPermission) {
            queryAndroidContactsDatabase(number)
        } else {
            null
        }

        return contact ?: querySupplementaryContacts(number)
    }

    private fun querySupplementaryContacts(phoneNumber: String): Contact? =
        preferences()
            .supplementaryContacts
            .find { supplementaryContact -> supplementaryContact.number == phoneNumber }
            ?.toContact()

    private fun queryAndroidContactsDatabase(phoneNumber: String): Contact? {
        val cursor = context.contentResolver.query(
            Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber),
            ),
            arrayOf(
                DISPLAY_NAME,
                PHOTO_URI,
            ),
            null,
            null,
            null,
        ) ?: return null

        return if (cursor.moveToFirst()) {
            val displayName = cursor.getString(0)
            val photoUri = cursor.getString(1)

            if (displayName.isNullOrBlank()) return null

            Contact(
                displayName,
                when {
                    photoUri.isNullOrBlank() -> null
                    else -> Uri.parse(photoUri)
                }
            )
        } else {
            null
        }.also { cursor.close() }
    }

    private val hasPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PERMISSION_GRANTED
}

data class Contact(val name: String, val imageUri: Uri?)
