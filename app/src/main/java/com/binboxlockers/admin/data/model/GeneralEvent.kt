package com.binboxlockers.admin.data.model

import com.binboxlockers.admin.ui.DoubleRowItem

/**
 * @author Chris Byers 12/22/20 - Copyright 2020 Binbox Lockers
 * Holds information about general events. For instance, the Atlanta Falcons at the Mercedes Benz
 * Stadium.  For lower level, instance-in-time events, see the Event class
 */
const val GENERAL_EVENT_DISPLAY_NAME_COL = "name"
const val LOCATION_COL = "location"

data class GeneralEvent(
    val displayName: String,
    val location: String
) : DoubleRowItem {
    override fun getTextRow1(): String {
        return displayName
    }

    override fun getTextRow2(): String {
        return location
    }

    override fun toString(): String {
        return "$displayName @ $location"
    }
}