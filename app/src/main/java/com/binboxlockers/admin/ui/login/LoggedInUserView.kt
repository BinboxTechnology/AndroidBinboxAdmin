package com.binboxlockers.admin.ui.login

/**
 * @author Chris Byers 12/22/20 - Copyright 2020 Binbox Lockers
 *
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val displayName: String
    //... other data fields that may be accessible to the UI
)