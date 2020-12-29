package com.binboxlockers.admin.ui.login

/**
 * @author Chris Byers 12/22/20 - Copyright 2020 Binbox Lockers
 *
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)