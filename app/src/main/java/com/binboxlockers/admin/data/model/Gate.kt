package com.binboxlockers.admin.data.model

import com.binboxlockers.admin.ui.DoubleRowItem

/**
 * @author Chris Byers 12/23/20 - Copyright 2020 Binbox Lockers
 */
const val GATES_TABLE = "Lockers"
const val GATE_NAME_ARRAY_COL = "gate"
const val GATE_LOCATION_COL = "locationName"
data class Gate(
    val name: String
) : DoubleRowItem {

    override fun toString(): String {
        return name
    }

    override fun getTextRow1(): String {
        return name
    }

    override fun getTextRow2(): String {
        return name
    }

}