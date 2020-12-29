package com.binboxlockers.admin.ui.lockers

/**
 * @author Chris Byers 12/23/20 - Copyright 2020 Binbox Lockers
 */

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.binboxlockers.admin.BinboxApplication
import com.binboxlockers.admin.BinboxApplication.Singleton.getBluetoothManager
import com.binboxlockers.admin.data.DataShare
import com.binboxlockers.admin.data.ParseDataSource
import com.binboxlockers.admin.data.ParseRepository
import com.binboxlockers.admin.data.model.Locker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LockersViewModel(private val parseRepository: ParseRepository) : ViewModel() {

    private val _lockers = MutableLiveData<List<Locker>>()
    val lockers: LiveData<List<Locker>> = _lockers

    private val _showSelectionNeededText = MutableLiveData<Boolean>(true)
    private val showSelectionNeededText: LiveData<Boolean> = _showSelectionNeededText

    init {
        val parseRepository = ParseRepository(ParseDataSource())
        _showSelectionNeededText.value = DataShare.lockers.isEmpty()
        _lockers.value = DataShare.lockers
    }

    fun openAllLockers(callback: (errors: List<String>) -> Unit) {
        val mgr = getBluetoothManager()
        val app = BinboxApplication.getAppContext()
        mgr.init(BinboxApplication.getAppContext())
        val errors: MutableList<String> = mutableListOf()
        GlobalScope.launch { // launch a new coroutine in background and continue
            DataShare.lockers.forEach { locker ->
                delay(500L) // non-blocking delay for 1 second (default time unit is ms)
                mgr.connect(locker.lockMACAddress) { result ->
                    println("Result: $result")
                    if (result != "Success") {
                        errors.add("Locker #${locker.userLockerNumber} error: $result")
                    }
                }
            }
            callback(errors)
        }
    }

    fun openSingleLocker(userLockerNumber: String, callback: (result: String) -> Void) {
        val mgr = getBluetoothManager()
        mgr.init(BinboxApplication.getAppContext())
        DataShare.lockers.forEach { locker ->
            if (locker.userLockerNumber == userLockerNumber) {
                mgr.connect(locker.lockMACAddress) { result ->
                    callback(result)
                }
            }
        }
    }

}