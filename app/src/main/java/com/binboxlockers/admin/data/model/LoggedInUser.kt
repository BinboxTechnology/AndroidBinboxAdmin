package com.binboxlockers.admin.data.model

/**
 * @author Chris Byers 12/23/20 - Copyright 2020 Binbox Lockers
 *
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val displayName: String,
    val isAdmin: Boolean
)