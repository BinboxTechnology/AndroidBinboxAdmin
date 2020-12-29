package com.binboxlockers.admin.ui.dashboard

/**
 * @author Chris Byers 12/23/20 - Copyright 2020 Binbox Lockers
 */

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.binboxlockers.admin.data.DataShare
import com.binboxlockers.admin.data.ParseRepository
import com.binboxlockers.admin.data.Result
import com.binboxlockers.admin.data.model.Event
import com.binboxlockers.admin.data.model.EventType
import com.binboxlockers.admin.data.model.Gate
import com.binboxlockers.admin.data.model.GeneralEvent

class DashboardViewModel(private val parseRepository: ParseRepository) : ViewModel() {

    var ignoreSelectionNotifications = false

    private val _selectedEventTypeIndex = MutableLiveData<Int>(-1)
    val selectedEventTypeIndex: LiveData<Int> = _selectedEventTypeIndex
    private val _selectedGeneralEventIndex = MutableLiveData<Int>(-1)
    val selectedGeneralEventIndex: LiveData<Int> = _selectedGeneralEventIndex
    private val _selectedEventIndex = MutableLiveData<Int>(-1)
    val selectedEventIndex: LiveData<Int> = _selectedEventIndex
    private val _selectedGateIndex = MutableLiveData<Int>(-1)
    val selectedGateIndex: LiveData<Int> = _selectedGateIndex

    private var _selectedGeneralEvent: GeneralEvent? = null
    private var _selectedEvent: Event? = null
    private var _selectedGate:Gate? = null

    private val _eventTypes = MutableLiveData<List<EventType>>(emptyList())
    val eventTypes: LiveData<List<EventType>> = _eventTypes

    private val _generalEvents = MutableLiveData<List<GeneralEvent>>(emptyList())
    val generalEvents: LiveData<List<GeneralEvent>> = _generalEvents

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _gates = MutableLiveData<List<Gate>>()
    val gates: LiveData<List<Gate>> = _gates

    init {
        if (DataShare.eventTypes.isEmpty()) {
            val result = parseRepository.getEventTypes()
            if (result is Result.Success) {
                _eventTypes.value = result.data
                DataShare.eventTypes = result.data
            }
        } else {
            ignoreSelectionNotifications = true
            println("Yep setting event types nwo ${DataShare.eventTypes}")
            _eventTypes.value = DataShare.eventTypes
            _generalEvents.value = DataShare.generalEvents
            _events.value = DataShare.events
            _gates.value = DataShare.gates
            _selectedEventTypeIndex.value = DataShare.selectedEventTypeIndex
            _selectedGeneralEventIndex.value = DataShare.selectedGeneralEventIndex
            _selectedEventIndex.value = DataShare.selectedEventIndex
            _selectedGateIndex.value = DataShare.selectedGateIndex
            ignoreSelectionNotifications = false
        }

    }

    fun handleEventTypeSelected(eventType: EventType, index: Int) {
        _selectedEventTypeIndex.value = index
        DataShare.selectedEventTypeIndex = index
        if (!ignoreSelectionNotifications)
            getGeneralEvents(eventType)
    }

    fun handleGeneralEventSelected(generalEvent: Any, index: Int) {
        _selectedGeneralEvent = generalEvent as GeneralEvent
        DataShare.selectedGeneralEvent = generalEvent as GeneralEvent
        DataShare.selectedGeneralEventIndex = index
        getEvents(_selectedGeneralEvent!!)
    }

    fun handleEventSelected(event: Any, index: Int) {
        _selectedEvent = event as Event
        DataShare.selectedEvent = event
        DataShare.selectedEventIndex = index
    }

    fun handleGateSelected(gate: Any, index: Int) {
        _selectedGate = gate as Gate
        DataShare.selectedGateIndex = index
        DataShare.selectedGate = gate
    }

    fun getGeneralEvents(eventType: EventType) {
        var result = parseRepository.getGeneralEvents(eventType)
        if (result is Result.Success)
            _generalEvents.value = result.data
    }

    fun getEvents(generalEvent: GeneralEvent) {
        var result = parseRepository.getEvents(generalEvent)
        if (result is Result.Success)
            _events.value = result.data
    }

    fun getGates(generalEvent: GeneralEvent) {
        val result = parseRepository.getGates(generalEvent)
        if (result is Result.Success)
            _gates.value = result.data
    }
}