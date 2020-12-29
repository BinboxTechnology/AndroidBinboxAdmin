package com.binboxlockers.admin.data

import android.util.Log
import com.binboxlockers.admin.data.model.*
import com.parse.ParseObject
import com.parse.ParseQuery
import java.lang.Exception
import java.util.*

/**
 * @author Chris Byers 12/21/20 - Copyright 2020 Binbox Lockers
 */
class ParseDataSource {

    fun getEventTypes(): Result<List<EventType>> {
        val query = ParseQuery.getQuery<ParseObject>(EVENT_TYPES_TABLE)
        val objects = query.find()
        val eventsList: MutableList<EventType> = ArrayList()
        for (o in objects) {
            val evt = EventType(o.getString(EVENT_TYPES_DISPLAY_NAME_COL) ?: "")
            eventsList.add(evt)
            println("Obj ${evt}")
        }

        return Result.Success(eventsList)
    }

    fun getGeneralEvents(eventType: EventType?): Result<List<GeneralEvent>> {
        val list: MutableList<GeneralEvent> = ArrayList()
        try {
            val query = ParseQuery.getQuery<ParseObject>(eventType?.displayName?.replace(Regex("\\s"), "")!!)
            query.limit = 1000
            val objects = query.find()
            for (o in objects) {
                val evt = GeneralEvent(
                    o.getString(GENERAL_EVENT_DISPLAY_NAME_COL) ?: "",
                    o.getString(LOCATION_COL) ?: ""
                )
                list.add(evt)
                println("Obj ${evt}")
            }
        } catch (e: Exception) {
            Log.e("Exc", e.toString())
        }
        return Result.Success(list)
    }

    fun getEvents(generalEvent: GeneralEvent?): Result<List<Event>> {
        val list: MutableList<Event> = ArrayList()
        try {
            println("Check event location ${generalEvent}")
            val query = ParseQuery.getQuery<ParseObject>(EVENTS_TABLE)
            query.limit = 1000
            query.whereEqualTo(EVENTS_LOCATION_COL, generalEvent?.location)
            query.whereEqualTo(EVENTS_EVENT_COL, generalEvent?.displayName)
            val objects = query.find()
            for (o in objects) {
                val evt = Event(
                    o.getString(EVENTS_DISPLAY_NAME_COL) ?: "",
                    o.getString(EVENTS_LOCATION_COL) ?: "",
                    o.getDate(EVENTS_DATE_COL) ?: Date()
                )
                list.add(evt)
                println("Obj ${evt}")
            }
        } catch (e: Exception) {
            Log.e("Exc", e.toString())
        }
        return Result.Success(list)
    }

    fun getGates(generalEvent: GeneralEvent?): Result<List<Gate>> {
        val list: MutableList<Gate> = ArrayList()
        try {
            println("Looking for gate at ${generalEvent?.location}")
            val query = ParseQuery.getQuery<ParseObject>(GATES_TABLE)
            query.limit = 1000
            query.whereEqualTo(GATE_LOCATION_COL, generalEvent?.location)
            val objects = query.find()
            for (o in objects) {
                val gates = o.getJSONArray(GATE_NAME_ARRAY_COL)
                if (gates != null) {

                    for (i in 0 until gates.length()) {
                        val gate = gates.getString(i)
                        list.add(Gate(gate))
                    }
                }
//                list.add(gate)
//                println("Gate Obj: ${gate}")
            }
        } catch (e: Exception) {
            Log.e("Exc", e.toString())
        }
        return Result.Success(list)
    }

    fun getLockersForLocation(generalEvent: GeneralEvent?, gate: Gate?): Result<List<Locker>> {
        val list: MutableList<Locker> = ArrayList()

        try {
            val query = ParseQuery.getQuery<ParseObject>(LOCKERS_TABLE)
            query.limit = 1000
            query.whereEqualTo(LOCKER_LOCATION_COL, generalEvent?.location)
            if (gate != null)
                query.whereEqualTo(LOCKER_GATE_COL, gate.name)
            val objects = query.find()
            for (o in objects) {
                val locker = Locker(
                        o.getString(LOCKER_USER_LOCKER_NUM_COL) ?: "",
                        o.getString(LOCKER_MAC_COL) ?: "",
                        o.getString(LOCKER_SERIAL_NUM_COL) ?: "",
                        o.getString(LOCKER_LOCK_TYPE_COL) ?: "",
                        o.getString(LOCKER_USER_COL) ?: "",
                        o.getBoolean(LOCKER_AVAILABLE_COL) ?: false,
                        o.getString(LOCKER_SIZE_COL) ?: ""
                )
                list.add(locker)
                println("Obj ${locker}")
            }
        } catch (e: Exception) {
            Log.e("Exc", e.toString())
        }

        return Result.Success(list)
    }

}