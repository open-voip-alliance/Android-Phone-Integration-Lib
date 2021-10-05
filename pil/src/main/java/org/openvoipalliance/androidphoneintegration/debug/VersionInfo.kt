package org.openvoipalliance.androidphoneintegration.debug

import android.content.Context
import org.openvoipalliance.androidphoneintegration.R
import org.openvoipalliance.voiplib.VoIPLib

data class VersionInfo(
    val pil: String,
    val voipLib: String,
    val voip: String
) {
    override fun toString(): String {
        return "Version Info: Android Phone Integration Lib: $pil | Android VoIP Lib: $voipLib | Underlying VoIP Library: $voip"
    }

    companion object {
        fun build(context: Context, voipLib: VoIPLib) = VersionInfo(
            context.getString(R.string.pil_build_info_tag),
            context.getString(R.string.pil_build_info_voip_lib_version),
            if (voipLib.isReady) voipLib.version() else "Not initialized"
        )
    }
}