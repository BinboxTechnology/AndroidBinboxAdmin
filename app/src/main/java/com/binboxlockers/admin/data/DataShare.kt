package com.binboxlockers.admin.data

import com.binboxlockers.admin.data.model.*

/**
 * @author Chris Byers 12/23/20 - Copyright 2020 Binbox Lockers
 */
object DataShare {
    var selectedEventTypeIndex: Int = 0
    var selectedGeneralEventIndex: Int = 0
    var selectedEventIndex: Int = 0
    var selectedGateIndex: Int = 0
    var selectedEventType: EventType? = null
    var selectedGeneralEvent: GeneralEvent? = null
    var selectedEvent: Event? = null
    var selectedGate: Gate? = null
    var eventTypes: List<EventType> = emptyList()
    var generalEvents: List<GeneralEvent> = emptyList()
    var events: List<Event> = emptyList()
    var gates: MutableList<Gate> = mutableListOf()
    var lockers: List<Locker> = emptyList()
}