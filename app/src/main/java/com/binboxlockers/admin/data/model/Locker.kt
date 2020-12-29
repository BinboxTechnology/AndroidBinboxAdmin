package com.binboxlockers.admin.data.model

/**
 * @author Chris Byers 12/21/20 - Copyright 2020 Binbox Lockers
 */
const val LOCKERS_TABLE = "LockerNumbers"
const val LOCKER_LOCATION_COL = "location"
const val LOCKER_GATE_COL = "gate"
const val LOCKER_MAC_COL = "MAC"
const val LOCKER_USER_LOCKER_NUM_COL = "userLockerNumber"
const val LOCKER_AVAILABLE_COL = "available"
const val LOCKER_SERIAL_NUM_COL = "lockerNumber"
const val LOCKER_SIZE_COL = "size"
const val LOCKER_USER_COL = "user"
const val LOCKER_LOCK_TYPE_COL = "rentalType"
data class Locker(
    val userLockerNumber: String,
    val lockMACAddress: String,
    val lockSerialNumber: String,
    val lockType: String,
    val user: String,
    val available: Boolean,
    val size: String
)