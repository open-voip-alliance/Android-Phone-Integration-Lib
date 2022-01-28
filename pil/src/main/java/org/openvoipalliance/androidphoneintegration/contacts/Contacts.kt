package org.openvoipalliance.androidphoneintegration.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import androidx.core.content.ContextCompat
import contacts.core.Contacts
import contacts.core.equalTo
import contacts.core.or
import org.openvoipalliance.androidphoneintegration.helpers.identifier
import org.openvoipalliance.voiplib.model.Call

internal class Contacts(private val context: Context) {

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
        if (!hasPermission) return null

        val contact = Contacts(context)
            .query()
            .where {
                (Phone.Number equalTo number) or (Phone.NormalizedNumber equalTo number)
            }
            .include {
                setOf(Contact.PhotoUri, Contact.DisplayNamePrimary)
            }
            .limit(1)
            .find()
            .firstOrNull() ?: return null

        if (contact.displayNamePrimary.isNullOrBlank()) return null

        return Contact(contact.displayNamePrimary!!, contact.photoUri)
    }

    private val hasPermission: Boolean
        get() = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PERMISSION_GRANTED
}

data class Contact(val name: String, val imageUri: Uri?)
