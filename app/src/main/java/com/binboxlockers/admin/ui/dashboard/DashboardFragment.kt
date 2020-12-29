package com.binboxlockers.admin.ui.dashboard

/**
 * @author Chris Byers 12/23/20 - Copyright 2020 Binbox Lockers
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.binboxlockers.admin.R
import com.binboxlockers.admin.data.DataShare
import com.binboxlockers.admin.data.ParseDataSource
import com.binboxlockers.admin.data.ParseRepository
import com.binboxlockers.admin.data.Result
import com.binboxlockers.admin.data.model.Event
import com.binboxlockers.admin.data.model.EventType
import com.binboxlockers.admin.data.model.Gate
import com.binboxlockers.admin.data.model.GeneralEvent
import com.binboxlockers.admin.ui.DoubleRowArrayAdapter
import com.binboxlockers.admin.ui.DoubleRowItem

class DashboardFragment : Fragment() {

    private lateinit var eventTypeSpinner: Spinner
    private lateinit var generalEventSpinner: Spinner
    private lateinit var gateSpinner: Spinner
    private lateinit var totalLockersText: TextView
    private lateinit var totalRentedText: TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val parseRepository = ParseRepository(ParseDataSource())
        eventTypeSpinner = root.findViewById(R.id.spn_event_type_selector)
        generalEventSpinner = root.findViewById(R.id.spn_general_event_selector)
        gateSpinner = root.findViewById(R.id.spn_gate_selector)
        totalLockersText = root.findViewById(R.id.text_total_lockers)
        totalRentedText = root.findViewById(R.id.text_total_rented)

        if (DataShare.eventTypes.isEmpty()) {
            val result = parseRepository.getEventTypes()
            if (result is Result.Success) {
                DataShare.eventTypes = result.data
            }
            createSimpleAdapter(DataShare.eventTypes, eventTypeSpinner)
            DataShare.selectedEventTypeIndex = 0
            DataShare.selectedGeneralEventIndex = 0
            DataShare.selectedEventIndex = 0
            DataShare.selectedGateIndex = 0
        } else {
            createSimpleAdapter(DataShare.eventTypes, eventTypeSpinner)
            eventTypeSpinner.setSelection(DataShare.selectedEventTypeIndex, false)
            if (DataShare.generalEvents.isNotEmpty()) {
                createDoubleRowAdapter(DataShare.generalEvents, generalEventSpinner)
                generalEventSpinner.setSelection(DataShare.selectedGeneralEventIndex, false)
            }
            if (DataShare.gates.isNotEmpty()) {
                createSimpleAdapter(DataShare.gates, gateSpinner)
                gateSpinner.setSelection(DataShare.selectedGateIndex, false)
            }
            updateDashboardTotals()
        }

        // Re-create the adapter for general events when event type changes
        eventTypeSpinner.onItemSelectedListener = object: OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                // Display the selected item text on text view
                DataShare.selectedEventTypeIndex = position
                DataShare.selectedEventType = parent.getItemAtPosition(position) as EventType
                val result = parseRepository.getGeneralEvents(DataShare.selectedEventType)
                if (result is Result.Success) {
                    DataShare.generalEvents = result.data
                    createDoubleRowAdapter(result.data, generalEventSpinner)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>){}
        }

        // Re-create the adapter for events when general event changes
        generalEventSpinner.onItemSelectedListener = object: OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                // Display the selected item text on text view
                DataShare.selectedGeneralEventIndex = position
                DataShare.selectedGeneralEvent = parent.getItemAtPosition(position) as GeneralEvent
                val gates = parseRepository.getGates(DataShare.selectedGeneralEvent)
                if (gates is Result.Success) {

                    // Create a fake gate called "All" that will not filter any gates
                    DataShare.gates = mutableListOf(Gate("All"))
                    DataShare.gates.addAll(1, gates.data)
                    createSimpleAdapter(gates.data, gateSpinner)
                    createSimpleAdapter(DataShare.gates, gateSpinner)
                    val lockers = parseRepository.getLockersForLocation(DataShare.selectedGeneralEvent, DataShare.selectedGate)
                    if (lockers is Result.Success) {
                        DataShare.lockers = lockers.data
                        updateDashboardTotals()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>){}
        }

        // Save selected gate when gate changes
        gateSpinner.onItemSelectedListener = object: OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                // Display the selected item text on text view
                DataShare.selectedGateIndex = position
                DataShare.selectedGate = parent.getItemAtPosition(position) as Gate
                val lockers = parseRepository.getLockersForLocation(DataShare.selectedGeneralEvent, DataShare.selectedGate)
                if (lockers is Result.Success) {
                    DataShare.lockers = lockers.data
                    updateDashboardTotals()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>){}
        }

        return root
    }
    fun <T> createSimpleAdapter(list: List<T>, spinner: Spinner) {
        ArrayAdapter(
                activity?.applicationContext!!,
                android.R.layout.simple_spinner_item,
                list
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
    }

    fun createDoubleRowAdapter(list: List<DoubleRowItem>, spinner: Spinner) {
        DoubleRowArrayAdapter(
                activity?.applicationContext!!,
                list
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.double_row_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
    }

    fun updateDashboardTotals() {
        var totalRented = 0
        DataShare.lockers.forEach {
            if (!it.available)
                ++totalRented
        }
        totalLockersText.text = DataShare.lockers.size.toString()
        totalRentedText.text = totalRented.toString()
    }
}
