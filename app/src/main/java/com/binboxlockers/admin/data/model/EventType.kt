package com.binboxlockers.admin.data.model

/**
 * @author Chris Byers 12/22/20 - Copyright 2020 Binbox Lockers
 */
const val EVENT_TYPES_TABLE = "EventTypes"
const val EVENT_TYPES_DISPLAY_NAME_COL = "displayName"

data class EventType(
    val displayName: String
) {
    override fun toString(): String {
        return displayName
    }
}
