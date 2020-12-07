package org.openvoipalliance.androidplatformintegration.example.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import androidx.core.view.children
import androidx.core.view.forEach
import kotlinx.android.synthetic.main.dialer.view.*
import kotlinx.android.synthetic.main.fragment_dialer.*
import kotlinx.android.synthetic.main.fragment_dialer.view.*
import org.openvoipalliance.androidplatformintegration.example.R
import kotlin.properties.Delegates

class Dialer @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : TableLayout(context, attrs) {

    var onCallListener: OnCallListener? = null

    var digits: String by Delegates.observable("") {
        _, _, new ->
        digitEntryWindow.text = new
        backspace.visibility = if (new.isNotBlank()) View.VISIBLE else View.GONE
    }
        private set

    init {
        inflate(getContext(), R.layout.dialer, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        keypad.children.forEach {
            (it as ViewGroup).forEach {
                if (it is Button) {
                    it.setOnClickListener {
                        val button = it as Button
                        digits = "${digits}${button.text}"
                    }
                }
            }
        }

        backspace.setOnClickListener {
            if (digits.isNotBlank()) {
                digits = digits.substring(0, digits.length - 1)
            }
        }

        backspace.setOnLongClickListener {
            digits = ""
            true
        }

        callButton.setOnClickListener {
            onCallListener?.onCall(digits)
        }
    }

    fun interface OnCallListener {
        fun onCall(number: String)
    }
}
