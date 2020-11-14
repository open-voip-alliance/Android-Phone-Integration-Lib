package nl.vialer.voip.android.example.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_dialer.*
import nl.vialer.voip.android.R

class DialerFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dialer, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keypad.children.forEach {
            (it as ViewGroup).forEach {
                if (it is Button) {
                    it.setOnClickListener {
                        val button = it as Button
                        changeDigits("${digitEntryWindow.text}${button.text}")
                    }
                }
            }
        }

        backspace.setOnClickListener {
            if (digitEntryWindow.text.isNotBlank()) {
                changeDigits(digitEntryWindow.text.substring(0, digitEntryWindow.text.length - 1))
            }
        }

        backspace.setOnLongClickListener {
            changeDigits("")
            true
        }
    }

    fun changeDigits(digits: String) {
        digitEntryWindow.text = digits
        backspace.visibility = if (digitEntryWindow.text.isNotBlank()) View.VISIBLE else View.GONE
    }
}