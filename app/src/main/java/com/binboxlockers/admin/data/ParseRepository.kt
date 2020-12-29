package com.binboxlockers.admin.data

import com.binboxlockers.admin.data.model.*

/**
 * @author Chris Byers 12/21/20 - Copyright 2020 Binbox Lockers
 */
class ParseRepository(val dataSource: ParseDataSource) {

    var eventTypes: List<EventType> = emptyList()
        private set

    var generalEvents: List<GeneralEvent> = emptyList()
        private set

    var events: List<Event> = emptyList()
        private set

    var gates: List<Gate> = emptyList()
        private set

    var lockers: List<Locker> = emptyList()
        private set

    fun getGeneralEvents(eventType: EventType?): Result<List<GeneralEvent>> {
        val result = dataSource.getGeneralEvents(eventType)
        if (result is Result.Success)
            setGeneralEvents(result.data)
        return result
    }

    fun getEvents(generalEvent: GeneralEvent?): Result<List<Event>> {
        val result = dataSource.getEvents(generalEvent)
        if (result is Result.Success)
            setEvents(result.data)
        return result
    }

    fun getGates(generalEvent: GeneralEvent?): Result<List<Gate>> {
        val result = dataSource.getGates(generalEvent)
        if (result is Result.Success)
            setGates(result.data)
        return result
    }

    fun getLockersForLocation(generalEvent: GeneralEvent?, gate: Gate?): Result<List<Locker>> {

        val realGate = if (gate != null && gate.name == "All") null else gate
        val result = dataSource.getLockersForLocation(generalEvent, realGate)
        if (result is Result.Success)
            setLockers(result.data)
        return result
    }

    fun getEventTypes(): Result<List<EventType>> {
        val result = dataSource.getEventTypes()
        if (result is Result.Success)
            setEventTypes(result.data)
        else
            println("Result ${result}")
        return result
    }

    private fun setEventTypes(eventTypes: List<EventType>) {
        this.eventTypes = eventTypes
    }

    private fun setGeneralEvents(generalEvents: List<GeneralEvent>) {
        this.generalEvents = generalEvents
        println("Locations size " + generalEvents.size)
    }

    private fun setEvents(events: List<Event>) {
        this.events = events
        println("Locations size " + events.size)
    }

    private fun setGates(gates: List<Gate>) {
        this.gates = gates
        println("Locations size " + gates.size)
    }

    private fun setLockers(lockers: List<Locker>) {
        this.lockers = lockers
        println("Lockers size " + lockers.size)
    }

}