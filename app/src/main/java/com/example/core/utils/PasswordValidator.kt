package com.example.core.utils

/**
 * Shared password-policy check used by every "set a new password" flow
 * (change password, reset password). Mirrors the backend's
 * validate_password_strength() so client and server agree on the rules,
 * and returns the same wording shown in the password-requirements helper text.
 *
 * Returns null when the password is valid, or the first violated-rule message.
 */
object PasswordValidator {
    const val REQUIREMENTS_TEXT =
        "Password must be at least 8 characters with uppercase, lowercase, number, and special character."

    fun validate(password: String): String? {
        if (password.length < 8) return "Password must be at least 8 characters"
        if (password.none { it.isUpperCase() }) return "Password must include at least one uppercase letter"
        if (password.none { it.isLowerCase() }) return "Password must include at least one lowercase letter"
        if (password.none { it.isDigit() }) return "Password must include at least one number"
        if (password.all { it.isLetterOrDigit() }) return "Password must include at least one special character"
        return null
    }
}
