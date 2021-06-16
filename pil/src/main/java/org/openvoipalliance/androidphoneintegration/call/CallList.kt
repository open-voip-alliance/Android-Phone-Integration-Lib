package org.openvoipalliance.androidphoneintegration.call

import org.openvoipalliance.androidphoneintegration.helpers.identifier
import org.openvoipalliance.voiplib.model.Call

interface CallList: MutableList<Call>  {
    /**
     * Add a call to the call list if it doesn't already exist, and if we have not reached
     * the maximum calls limit.
     *
     */
    fun addCall(element: Call): Boolean

    /**
     * Remove a call from the call list and then return the removed element.
     *
     */
    fun removeCall(element: Call): Call?

    /**
     * Find a call in the call list.
     *
     */
    fun findCall(element: Call): Call?

    fun callExists(element: Call): Boolean
}

internal class CallArrayList(private val maxCalls: Int): ArrayList<Call>(), CallList {

    override fun addCall(element: Call): Boolean {
        if (callExists(element)) return false

        if (size > maxCalls) return false

        return super.add(element)
    }

    override fun removeCall(element: Call) = findCall(element)?.also { remove(it) }

    override fun findCall(element: Call) = firstOrNull {
        it.identifier == element.identifier
    }

    override fun callExists(element: Call) = any { element.identifier == it.identifier }
}