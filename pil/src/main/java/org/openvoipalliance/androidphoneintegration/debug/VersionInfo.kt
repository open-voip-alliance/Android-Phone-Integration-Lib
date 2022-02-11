package org.openvoipalliance.androidphoneintegration.debug

import android.content.Context
import org.openvoipalliance.androidphoneintegration.R
import org.openvoipalliance.voiplib.VoIPLib

data class VersionInfo(
    val pil: String,
    val voip: String
) {
    override fun toString(): String {
        return "Version Info: Android Phone Integration Lib: $pil | Underlying VoIP Library: $voip"
    }

    companion object {
        internal fun build(context: Context, voipLib: VoIPLib) = VersionInfo(
            context.getString(R.string.pil_build_info_tag),
            if (voipLib.isInitialized) voipLib.version else "Not initialized"
        )
    }
}