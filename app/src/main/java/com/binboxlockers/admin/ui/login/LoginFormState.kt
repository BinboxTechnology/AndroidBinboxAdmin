package com.binboxlockers.admin.ui.login

/**
 * @author Chris Byers 12/22/20 - Copyright 2020 Binbox Lockers
 *
 * Data validation state of the login form.
 */
data class LoginFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)