package org.openvoipalliance.androidphoneintegration.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import androidx.core.content.ContextCompat
import com.tomash.androidcontacts.contactgetter.main.FieldType
import com.tomash.androidcontacts.contactgetter.main.contactsGetter.ContactsGetterBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class Contacts(private val context: Context) {

    fun find(number: String): Contact? {
        if (!hasPermission) return null

        val contact = ContactsGetterBuilder(context)
            .onlyWithPhones()
            .addField(FieldType.NAME_DATA)
            .withPhone(number)
            .firstOrNull()
            ?: return null

        return Contact(contact.nameData.fullName, contact.photoUri)
    }

    suspend fun findAsync(number: String): Contact? = withContext(Dispatchers.IO) {
        find(number)
    }

    private val hasPermission: Boolean
        get() = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PERMISSION_GRANTED
}

data class Contact(val name: String, val imageUri: Uri)
