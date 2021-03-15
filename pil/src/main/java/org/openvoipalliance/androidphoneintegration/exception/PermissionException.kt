package org.openvoipalliance.androidphoneintegration.exception

class PermissionException internal constructor(missingPermission: String) : Exception(
    "Missing required permission: $missingPermission"
)
