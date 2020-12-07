package org.openvoipalliance.androidplatformintegration.exception

class PermissionException internal constructor(missingPermission: String) : Exception(
    "Missing required permission: $missingPermission"
)
