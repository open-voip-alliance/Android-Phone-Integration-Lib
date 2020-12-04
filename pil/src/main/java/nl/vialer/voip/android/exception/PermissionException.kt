package nl.vialer.voip.android.exception

class PermissionException internal constructor(missingPermission: String) : Exception(
    "Missing required permission: $missingPermission"
)
