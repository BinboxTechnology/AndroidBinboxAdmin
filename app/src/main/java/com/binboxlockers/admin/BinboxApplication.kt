package com.binboxlockers.admin

/**
 * @author Chris Byers 12/21/20 - Copyright 2020 Binbox Lockers
 */

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.binboxlockers.admin.data.BluetoothManager
import com.parse.Parse

class BinboxApplication: MultiDexApplication() {

    init {
        instance = this
    }

    companion object Singleton {
        private lateinit var bluetoothManager: BluetoothManager
        private var instance: Application? = null
        fun getBluetoothManager() = bluetoothManager
        fun getAppContext() = instance!!
    }

    override fun onCreate() {
        super.onCreate()

        bluetoothManager = BluetoothManager()
        bluetoothManager.init(this)
        bluetoothManager.start()
        Parse.initialize(Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id)) // if defined
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        )
    }
}