package com.binboxlockers.admin.ui.lockers

/**
 * @author Chris Byers 12/23/20 - Copyright 2020 Binbox Lockers
 */

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.binboxlockers.admin.BinboxApplication
import com.binboxlockers.admin.R
import com.binboxlockers.admin.ui.LockersArrayAdapter

class LockersFragment : Fragment() {

    private lateinit var lockersViewModel: LockersViewModel
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requestBluetoothPermissions()
        lockersViewModel =
                ViewModelProvider(this, LockersViewModelFactory()).get(LockersViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_lockers, container, false)
        val btnUnlockAll: Button = root.findViewById(R.id.btn_open_all_lockers)
        btnUnlockAll.setOnClickListener(View.OnClickListener {
            lockersViewModel.openAllLockers {
                if (it.isNotEmpty()) {
                    requireActivity().runOnUiThread(Runnable {
                        Toast.makeText(
                            BinboxApplication.getAppContext(),
                            "Error opening some lockers",
                            Toast.LENGTH_LONG
                        ).show()
                    })
                }
            }
        })

        val listView = root.findViewById<ListView>(R.id.lv_lockers)
        lockersViewModel.lockers.observe(viewLifecycleOwner, Observer {
            val adapter = LockersArrayAdapter(activity?.applicationContext!!, it, {
                lockersViewModel.openSingleLocker(it) { result ->
                    println("Result $result")
                    requireActivity().runOnUiThread(Runnable {
                        Toast.makeText(
                                BinboxApplication.getAppContext(),
                                result,
                                Toast.LENGTH_LONG
                        ).show()
                    })
                }
            }, { locker ->
                requireActivity().runOnUiThread(Runnable {
                    Toast.makeText(
                            BinboxApplication.getAppContext(),
                            locker.user,
                            Toast.LENGTH_LONG
                    ).show()
                })
            })
            listView.adapter = adapter
        })

        return root
    }

    fun requestBluetoothPermissions() {
        val permission = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)

        println("Permission is $permission")
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                100)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            100 -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("Permission", "Permission has been denied by user")
                } else {
                    Log.i("Permission", "Permission has been granted by user")
                }
            }
        }
    }
}