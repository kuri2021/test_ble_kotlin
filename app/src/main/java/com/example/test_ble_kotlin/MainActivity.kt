package com.example.test_ble_kotlin

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

val ScanFilterService_UUID : ParcelUuid = ParcelUuid.fromString("00001101-0000-1000-8000-00805f9b34fb")

class MainActivity : AppCompatActivity() {

    private val TAG="MainActivity"

    private val REQUEST_ENABLE_BT=1
    private val REQUEST_ALL_PERMISSION= 2
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanning: Boolean = false
    private var devicesArr = ArrayList<BluetoothDevice>()
    private val SCAN_PERIOD = 1000
    private val handler = Handler()
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerViewAdapter : RecyclerViewAdapter

    @SuppressWarnings("MissingPermission")
    private val mLeScanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("scanCallback", "BLE Scan Failed : " + errorCode)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.let{
                // results is not null
                for (result in it){
                    if (!devicesArr.contains(result.device) && result.device.name!=null) devicesArr.add(result.device)
                }
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "onScanResult: single ${result.toString()}")
//            Log.d(TAG, "onScanResult: single ${result.}")
            result?.let {
                // result is not null
                if (!devicesArr.contains(it.device) && it.device.name!=null) devicesArr.add(it.device)
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }

    }

    private fun buildScanFilters(): List<ScanFilter> {
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ScanFilterService_UUID)
            .build()
        Log.d(TAG, "buildScanFilters")
        return listOf(scanFilter)
    }

    private fun buildScanSettings() = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    @SuppressWarnings("MissingPermission")
    private fun scanDevice(state:Boolean) = if(state){
        handler.postDelayed({
            scanning = false
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
        }, SCAN_PERIOD.toLong())
        scanning = true
        devicesArr.clear()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)
        bluetoothAdapter?.bluetoothLeScanner?.startScan(buildScanFilters(), buildScanSettings(),mLeScanCallback)

        try {
            val getUuidsMethod: Method = BluetoothAdapter::class.java.getDeclaredMethod("getUuids", null)
            val uuids = getUuidsMethod.invoke(bluetoothAdapter, null) as Array<ParcelUuid>
            if (uuids != null) {
                for (uuid in uuids) {
                    Log.d(TAG, "UUID: ${uuid}" + uuid.uuid.toString())
                }
            } else {
                Log.d(TAG, "Uuids not found, be sure to enable Bluetooth!")
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            Log.d(TAG, "UUIDF:" + e.toString())
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            Log.d(TAG, "UUIDF:" + e.toString())
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            Log.d(TAG, "UUIDF:" + e.toString())
        }
    }else{
        scanning = false
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
    }

    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bleOnOffBtn:ToggleButton = findViewById(R.id.ble_on_off_btn)
        val scanBtn: Button = findViewById(R.id.scanBtn)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        viewManager = LinearLayoutManager(this)
        recyclerViewAdapter =  RecyclerViewAdapter(devicesArr)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = viewManager
            adapter = recyclerViewAdapter
        }

        if(bluetoothAdapter!=null){
            if(bluetoothAdapter?.isEnabled==false){
                bleOnOffBtn.isChecked = true
                scanBtn.isVisible = false
            } else{
                bleOnOffBtn.isChecked = false
                scanBtn.isVisible = true
            }
        }

        bleOnOffBtn.setOnCheckedChangeListener { _, isChecked ->
            bluetoothOnOff()
            scanBtn.visibility = if (scanBtn.visibility == View.VISIBLE){ View.INVISIBLE } else{ View.VISIBLE }
        }

        scanBtn.setOnClickListener { v:View? -> // Scan Button Onclick
            if (!hasPermissions(this, PERMISSIONS)) {
                requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
            }
            scanDevice(true)
        }


    }

    @SuppressWarnings("MissingPermission")
    fun bluetoothOnOff(){
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d("bluetoothAdapter","Device doesn't support Bluetooth")
        }else{
            if (bluetoothAdapter?.isEnabled == false) { // 블루투스 꺼져 있으면 블루투스 활성화
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else{ // 블루투스 켜져있으면 블루투스 비활성화
                bluetoothAdapter?.disable()
            }
        }
    }

}
private fun Handler.postDelayed(function: () -> Unit?, scanPeriod: Int) {

}