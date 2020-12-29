package com.binboxlockers.admin.data.model

import com.binboxlockers.admin.ui.DoubleRowItem
import java.util.*

/**
 * @author Chris Byers 12/22/20 - Copyright 2020 Binbox Lockers
 * This holds information about an instance-in-time event, such as a specific ballgame at specific
 * time at a field (location).
 * location matches GeneralEvent.location
 */
const val EVENTS_TABLE = "Events"
const val EVENTS_DISPLAY_NAME_COL = "name"
const val EVENTS_LOCATION_COL = "eventName"
const val EVENTS_EVENT_COL = "event"
const val EVENTS_DATE_COL = "date"

data class Event(
    val displayName: String,
    val location: String,
    val date: Date
) : DoubleRowItem {
    override fun getTextRow1(): String {
        return displayName
    }

    override fun getTextRow2(): String {
        return date.toString()
    }

}