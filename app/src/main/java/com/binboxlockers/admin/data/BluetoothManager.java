package com.binboxlockers.admin.data;

import android.app.Application;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.internal.RxBleLog;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * {@link BluetoothManager} implementation.
 */
public class BluetoothManager {

    public interface ConnectionCallback {
        public void onConnectionStatus(String status);
    }

    private Application mApplication;
    private RxBleClient mClient;
    private CompositeDisposable mDisposables;
    private Map<String, RxBleDevice> mScannedDevicesMap;
    private ConnectionCallback mCallback;

    private String connectAddress;
    private int connectAttempts = 0;

    // Old Locks
    private static UUID CT_ID_UUID;
    private static UUID CT_COMMAND_UUID;
    private static UUID CT_BLE_SERVICE_UUID;

    // New Locks
    private static UUID KER_CT_ID_UUID;
    private static UUID KER_CT_COMMAND_UUID;
    private static UUID KER_CT_BLE_SERVICE_UUID;

    private byte mCommKey;
    private ConnectionCharacteristicsWrapper mWrapper;
    private Disposable mLonelyDisposable;

    private class Opener extends Thread {
        private ArrayList<String> _macAddresses;
        private ArrayList<String> _errors = new ArrayList<>();

        public Opener(ArrayList<String> macAddresses) {
            super();
            _macAddresses = macAddresses;
        }

        @Override
        public void run() {
            _errors.clear();
            for (int i=0; i < _macAddresses.size(); ++i) {

            }
        }
    }

    public BluetoothManager() {
        mScannedDevicesMap = new HashMap<>();
        CT_BLE_SERVICE_UUID = UUID.fromString("6E400001-E6AC-A7E7-B1B3-E699BAE80060");
        CT_ID_UUID = UUID.fromString("6E400002-E6AC-A7E7-B1B3-E699BAE80060");
        CT_COMMAND_UUID = UUID.fromString("6E400003-E6AC-A7E7-B1B3-E699BAE80060");
        KER_CT_BLE_SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
        KER_CT_ID_UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
        KER_CT_COMMAND_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");   // fff1 and 27 and 2a02
    }

    public void init(Application application) {
        mApplication = application;
        mClient = RxBleClient.create(mApplication);
        mDisposables = new CompositeDisposable();
        RxBleClient.setLogLevel(RxBleLog.DEBUG);
    }

    public void start() {
        mLonelyDisposable = (mClient.observeStateChanges()
                .startWith(mClient.getState())
                .switchMap((Function<RxBleClient.State, ObservableSource<ScanResult>>) state -> {
                    switch (state) {
                        case READY:
                            // Np functionality will work under the following.
                        case BLUETOOTH_NOT_AVAILABLE:
                        case LOCATION_PERMISSION_NOT_GRANTED:
                        case BLUETOOTH_NOT_ENABLED:
                        case LOCATION_SERVICES_NOT_ENABLED:
                        default:
                            return Observable.empty();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processScanResult, this::processError));
    }

    private Observable<ScanResult> startScanning(String address) {

        return mClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build(),
                new ScanFilter.Builder()
                        .setDeviceAddress(address)
                        .build()
        );
    }

    private void processScanResult(ScanResult scanResult) {
        RxBleDevice device = scanResult.getBleDevice();
        mDisposables.add(device.establishConnection(false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processConnection, this::processError));
    }

    public void destroy() {
        if (mLonelyDisposable != null && !mLonelyDisposable.isDisposed()) {
            mLonelyDisposable.dispose();
        }
        if (mDisposables != null) {
            mDisposables.clear();
        }
    }

    public void connect(String address, ConnectionCallback callback) {
        mCallback = callback;
        connectAddress = address;

        try {
            Log.d("connect", "Connecting to device: " + address);

            mDisposables.add(startScanning(address)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::processScanResult, this::processError));
        } catch (UnsupportedOperationException e) {
//            Toast.makeText(mApplication, "Your device does not support bluetooth, or it is not enabled", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
//            Toast.makeText(mApplication, "Cannot find locker near you", Toast.LENGTH_SHORT).show();
//            Toast.makeText(mApplication, "Error:" + e.toString(), Toast.LENGTH_LONG).show();
            processError(e);
        }
    }

    private void processConnection(RxBleConnection connection) {
        Log.d("BleBluetoothManager", "Connected");
        mDisposables.add(Observable.fromCallable(() -> buildWrapper(connection))
                .map(this::processServices)
                .map(this::setUpNotifications)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::talk, this::processError));
    }


    private ConnectionCharacteristicsWrapper buildWrapper(RxBleConnection connection) {
        Log.d("BleBluetoothManager", "Build Wrapper");
        ConnectionCharacteristicsWrapper wrapper = new ConnectionCharacteristicsWrapper();
        wrapper.setConnection(connection);
        wrapper.setDeviceServices(connection.discoverServices().blockingGet());
        return wrapper;
    }

    private ConnectionCharacteristicsWrapper processServices(ConnectionCharacteristicsWrapper wrapper) {
        Log.d("BleBluetoothManager", "Process services");
        boolean command = false;
        boolean id = false;
        BluetoothGattCharacteristic idCharacteristic = null;
        if (wrapper.getDeviceServices() != null && wrapper.getDeviceServices().getBluetoothGattServices() != null) {

            for (BluetoothGattService service : wrapper.getDeviceServices().getBluetoothGattServices()) {
                if (CT_BLE_SERVICE_UUID.toString().equalsIgnoreCase(service.getUuid().toString()) ||
                        KER_CT_BLE_SERVICE_UUID.toString().equalsIgnoreCase(service.getUuid().toString())) {

                    if (service.getCharacteristics() != null) {
                        for (BluetoothGattCharacteristic bluetoothGattCharacteristic : service.getCharacteristics()) {
                            if (CT_COMMAND_UUID.toString().equalsIgnoreCase(bluetoothGattCharacteristic.getUuid().toString()) ||
                                    KER_CT_COMMAND_UUID.toString().equalsIgnoreCase(bluetoothGattCharacteristic.getUuid().toString())) {
                                command = true;
                                if (KER_CT_COMMAND_UUID.toString().equalsIgnoreCase(bluetoothGattCharacteristic.getUuid().toString()))
                                    wrapper.setIsNewLock(true);
                            }
                            if (CT_ID_UUID.toString().equalsIgnoreCase(bluetoothGattCharacteristic.getUuid().toString()) ||
                                    KER_CT_ID_UUID.toString().equalsIgnoreCase(bluetoothGattCharacteristic.getUuid().toString())) {
                                id = true;
                                idCharacteristic = bluetoothGattCharacteristic;
                            }
                        }
                    }
                }
            }
        }
        if (command && id) {
            wrapper.setIdCharacteristic(idCharacteristic);
            return wrapper;
        }

        return null;
    }

    private ConnectionCharacteristicsWrapper setUpNotifications(ConnectionCharacteristicsWrapper wrapper) {
        if (wrapper == null) {
            processError(new Exception("Error processing services"));
            return null;
        }

        if (wrapper.getIsNewLock()) {
            // New Locks
            mDisposables.add(wrapper.getConnection().setupNotification(KER_CT_COMMAND_UUID)
                    .flatMap(notificationObservable -> notificationObservable) // <-- Notification has been set up, now observe value changes.
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::processNotification, this::processError));
        } else {
            // Old Locks
            mDisposables.add(wrapper.getConnection().setupNotification(CT_COMMAND_UUID)
                    .flatMap(notificationObservable -> notificationObservable) // <-- Notification has been set up, now observe value changes.
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::processNotification, this::processError));
        }

        return wrapper;
    }

    private void talk(ConnectionCharacteristicsWrapper wrapper) throws Exception {
        Log.d("BleBluetoothManager", "Talking");

        if (wrapper == null) {
            processError(new Exception("Failed to communicate with device."));
            return;
        }

        byte[] fin_bytes = getWriteData(wrapper.getIsNewLock());
//        Log.d("BleBluetoothManager", Arrays.toString(fin_bytes));

        mDisposables.add(wrapper.getConnection().writeCharacteristic(wrapper.getIdCharacteristic(), fin_bytes)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) throws Exception {
                        Log.d("BleBluetoothManager", "WRITE SUCCESS");
//                        Log.d("BleBluetoothManager", Arrays.toString(bytes));
                        mWrapper = wrapper;
                    }
                }, this::processError));
    }

    private void openCabKerLock(byte key) {
        int temp = 0xF5 + 0x61 + 0x00 + 0x01 + 0x5F + (0x36 ^ key);
        byte checksum = (byte)(temp & 0xFF);
        byte[] data = new byte[] {(byte)0xF5, 0x61, 0x00, 0x01, 0x5F, checksum, (byte)(0x36 ^ key)};
        if (mWrapper == null) {
            processError(new Exception("Failed to communicate with device."));
            return;
        }
        Log.d("BleBluetoothManager", "openCabKerLock");

        mDisposables.add(mWrapper.getConnection().writeCharacteristic(mWrapper.getIdCharacteristic(), data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) throws Exception {
                        Log.d("BleBluetoothManager", "WRITE SUCCESS");
                        mCallback.onConnectionStatus("Success");
                        final Handler handler = new Handler();
                        handler.postDelayed(() -> mDisposables.clear(), 6000);
                    }
                }, this::processError));
    }

    private void openLock(byte key) {
        byte[] data = new byte[15];
        try {
            if (mWrapper == null) {
                processError(new Exception("Failed to communicate with device."));
                return;
            }
            data = getWriteDataOpenLock(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("BleBluetoothManager", "openLock");

        mDisposables.add(mWrapper.getConnection().writeCharacteristic(mWrapper.getIdCharacteristic(), data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) throws Exception {
                        Log.d("BleBluetoothManager", "WRITE SUCCESS");
                        mCallback.onConnectionStatus("Success");
                        mDisposables.clear();
                    }
                }, this::processError));
    }


    private void processNotification(byte[] bytes) {
        Log.d("BleBluetoothManager", "Notification: " + Arrays.toString(bytes));
        byte randNumber = (byte) (bytes[3] - 0x32);

        // Old Locks
        if ((bytes[5] ^ randNumber) == 0x01) { //Lock connection
            mCommKey = (byte) (bytes[4] ^ randNumber);
            openLock(mCommKey);
        }

        // New Locks
        else if (bytes[1] == 0x60 && bytes[2] == 0x10) {
            mCommKey = bytes[14];
            openCabKerLock(mCommKey);
        }
    }

    private byte[] getWriteData(boolean isNewLock) throws IOException {

        byte[] fin_bytes = null;
        if (isNewLock) {
            int temp = 0xF5 + 0x60 + 0x00 + 0x00 + 0x5F;
            byte checksum = (byte)(temp & 0xFF);
            fin_bytes = new byte[] {(byte)0xF5, 0x60, 0x00, 0x00, 0x5F, (byte)0xB4};
        } else {

            byte rand = (byte) (new Random().nextInt(99) + 2); //ThreadLocalRandom.current().nextInt(2, 100 + 1);
            byte rand_1 = (byte) (rand + 0x32);

            byte[] bytesArray = {(byte) 0xA3, (byte) 0xA4, (byte) 0x08, rand_1, 0x00, (byte) (0x01 ^ rand), (byte) (0x4F ^ rand), (byte) (0x6D ^ rand),
                    (byte) (0x6E ^ rand), (byte) (0x69 ^ rand), (byte) (0x57 ^ rand), (byte) (0x34 ^ rand), (byte) (0x47 ^ rand), (byte) (0x58 ^ rand)};
            byte crc8 = crcCheck(bytesArray, bytesArray.length);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output.write(bytesArray);
            output.write(crc8);
            fin_bytes = output.toByteArray();
        }

        return fin_bytes;
    }

    private byte[] getWriteDataOpenLock(byte key) throws IOException {

        byte rand = (byte) (new Random().nextInt(99) + 2); //ThreadLocalRandom.current().nextInt(2, 100 + 1);
        byte rand_1 = (byte) (rand + 0x32);

        int unixTime = (int) (System.currentTimeMillis() / 1000L);


        byte[] bytesArray = {(byte) 0xA3, (byte) 0xA4, (byte) 0x0A, rand_1, (byte) (key ^ rand), (byte) (0x05 ^ rand), (byte) (0x01 ^ rand), (byte) (0x00001AFC ^ rand), (byte) ((unixTime & 0xFFFFFFFF) ^ rand), (byte) (0x00 ^ rand)};


        byte crc8 = crcCheck(bytesArray, bytesArray.length);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(bytesArray);
        output.write(crc8);
        byte[] fin_bytes = output.toByteArray();

        return fin_bytes;
    }

    private byte crcCheck(byte[] data, int length) {
        byte crc8 = 0;
        for (int i = 0; i < length; i++) {
            crc8 = (byte) CRC8Table[(crc8 ^ data[i]) & 0xFF];
        }
        return crc8;
    }

    private char[] CRC8Table = {0, 94, 188, 226, 97, 63, 221, 131, 194, 156, 126, 32, 163, 253, 31, 65, 157,
            195, 33, 127, 252, 162, 64, 30, 95, 1, 227, 189, 62, 96, 130, 220, 35, 125, 159, 193, 66,
            28, 254, 160, 225, 191, 93, 3, 128, 222, 60, 98, 190, 224, 2, 92, 223, 129, 99, 61, 124,
            34, 192, 158, 29, 67, 161, 255, 70, 24, 250, 164, 39, 121, 155, 197, 132, 218, 56, 102,
            229, 187, 89, 7, 219, 133, 103, 57, 186, 228, 6, 88, 25, 71, 165, 251, 120, 38, 196, 154,
            101, 59, 217, 135, 4, 90, 184, 230, 167, 249, 27, 69, 198, 152, 122, 36, 248, 166, 68, 26,
            153, 199, 37, 123, 58, 100, 134, 216, 91, 5, 231, 185, 140, 210, 48, 110, 237, 179, 81,
            15, 78, 16, 242, 172, 47, 113, 147, 205, 17, 79, 173, 243, 112, 46, 204, 146, 211, 141,
            111, 49, 178, 236, 14, 80, 175, 241, 19, 77, 206, 144, 114, 44, 109, 51, 209, 143, 12,
            82, 176, 238, 50, 108, 142, 208, 83, 13, 239, 177, 240, 174, 76, 18, 145, 207, 45, 115,
            202, 148, 118, 40, 171, 245, 23, 73, 8, 86, 180, 234, 105, 55, 213, 139, 87, 9, 235, 181,
            54, 104, 138, 212, 149, 203, 41, 119, 244, 170, 72, 22, 233, 183, 85, 11, 136, 214, 52,
            106, 43, 117, 151, 201, 74, 20, 246, 168, 116, 42, 200, 150, 21, 75, 169, 247, 182, 232,
            10, 84, 215, 137, 107, 53};


    private void processError(Throwable throwable) {
        Log.e("BleBluetoothManager", throwable.getMessage());
        throwable.printStackTrace();

        // Attempt multiple connections
        if (throwable.getMessage().contains("133")) {
            if (connectAttempts++ < 4) {
                Log.e("ConnectFail", "Attempt # " + connectAttempts);
                connect(connectAddress, mCallback);
                return;
            }
        }

        connectAttempts = 0;
        if (mCallback != null) {
            try {
                mCallback.onConnectionStatus("Error");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mDisposables.clear();
    }

    public void clear() {
        if (mDisposables != null) {
            mDisposables.clear();
        }
    }

    private static class ConnectionCharacteristicsWrapper {
        private RxBleConnection connection;
        private BluetoothGattCharacteristic idCharacteristic;
        private RxBleDeviceServices deviceServices;
        private boolean isNewLock = false;

        public ConnectionCharacteristicsWrapper() {
        }

        public RxBleConnection getConnection() {
            return connection;
        }

        public BluetoothGattCharacteristic getIdCharacteristic() {
            return idCharacteristic;
        }

        public RxBleDeviceServices getDeviceServices() {
            return deviceServices;
        }

        public void setConnection(RxBleConnection connection) {
            this.connection = connection;
        }

        public void setIdCharacteristic(BluetoothGattCharacteristic idCharacteristic) {
            this.idCharacteristic = idCharacteristic;
        }

        public void setDeviceServices(RxBleDeviceServices deviceServices) {
            this.deviceServices = deviceServices;
        }

        public void setIsNewLock(boolean isNewLock) {
            this.isNewLock = isNewLock;
        }

        public boolean getIsNewLock() { return this.isNewLock; }
    }
}