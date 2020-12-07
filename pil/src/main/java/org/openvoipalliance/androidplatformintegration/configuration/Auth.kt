package org.openvoipalliance.androidplatformintegration.configuration

data class Auth(
    val username: String,
    val password: String,
    val domain: String,
    val port: Int,
    val secure: Boolean
) {
    val isValid: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && domain.isNotBlank() && port != 0
}
