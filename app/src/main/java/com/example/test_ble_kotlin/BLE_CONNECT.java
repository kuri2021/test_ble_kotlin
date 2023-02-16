package com.example.test_ble_kotlin;

import static android.service.controls.ControlsProviderService.TAG;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BLE_CONNECT {
    private BluetoothGatt mBluetoothGatt;

    private BluetoothGattCharacteristic mReadCharacteristic = null;
    private BluetoothGattCharacteristic mWriteCharateristic = null;

    private final String SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    private final String WRITE_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";
    private final String READ_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";

    @SuppressWarnings("MissingPermission")
    private boolean findGattServices() {
        List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();
        if (gattServices == null) return false;

        mReadCharacteristic = null;
        mWriteCharateristic = null;
        for (BluetoothGattService gattService : gattServices){
            HashMap<String, String> currentServiceData = new HashMap<String, String>();

            if(gattService.getUuid().toString().equals(SERVICE)){
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics){
                    if(gattCharacteristic.getUuid().toString().equals(READ_UUID)){
                        final int charaProp = gattCharacteristic.getProperties();

                        if((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){
                            try{
                                mReadCharacteristic = gattCharacteristic;
                                List<BluetoothGattDescriptor> list = mReadCharacteristic.getDescriptors();
                                Log.d(TAG, "read characteristic found : " + charaProp);

                                mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
                                //리시버 설정
                                BluetoothGattDescriptor descriptor = mReadCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                mBluetoothGatt.writeDescriptor(descriptor);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                                return false;
                            }
                        }
                        else{
                            Log.d(TAG, "read characteristic prop is invalid : " + charaProp);
                        }
                    }
                    else if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(WRITE_UUID)){
                        final int charaProp = gattCharacteristic.getProperties();

                        if((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0){
                            Log.d(TAG, "write characteristic found : " + charaProp);
                            mWriteCharateristic = gattCharacteristic;
                        }
                        else{
                            Log.d(TAG, "write characteristic prop is invalid : " + charaProp);
                        }
                    }
                }
            }
        }
        return true;
    }

}
