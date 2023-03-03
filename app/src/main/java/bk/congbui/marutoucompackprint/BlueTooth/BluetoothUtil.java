package bk.congbui.marutoucompackprint.BlueTooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.example.myapplication.R;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


/**
 *  Simple package for connecting a sunmi printer via Bluetooth
 */
public class BluetoothUtil {

    private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

//    public static final String Innerprinter_Address = "00:01:90:BA:8C:63";

    public static String NameDevice = "BLM-80";

    public static boolean isBlueToothPrinter = false;

    private static BluetoothSocket bluetoothSocket;

    public static BluetoothAdapter getBTAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    @SuppressLint("MissingPermission")
    private static BluetoothDevice getDevice(BluetoothAdapter bluetoothAdapter) {
        BluetoothDevice innerprinter_device = null;
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getName().equals(NameDevice)) {
                innerprinter_device = device;
                break;
            }
        }
        return innerprinter_device;
    }

    @SuppressLint("MissingPermission")
    private static BluetoothSocket getSocket(BluetoothDevice device) throws IOException {
        BluetoothSocket socket;
        if (device !=  null){
            socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
            socket.connect();
            return  socket;
        }else {
            return null;
        }
    }

    /**
     * connect bluetooth
     */
    public static boolean connectBlueTooth(Context context) throws IOException {
            bluetoothSocket = getSocket(getDevice(getBTAdapter()));

        if (bluetoothSocket == null) {
            if (getBTAdapter() == null) {
                Toast.makeText(context,  R.string.toast_3, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (!getBTAdapter().isEnabled()) {
                Toast.makeText(context, R.string.toast_4, Toast.LENGTH_SHORT).show();
                return false;
            }
            BluetoothDevice device;
            if ((device = getDevice(getBTAdapter())) == null) {
                Toast.makeText(context, R.string.toast_5, Toast.LENGTH_SHORT).show();
                return false;
            }

            try {
                bluetoothSocket = getSocket(device);
                Toast.makeText(context, "Device Connected", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                Toast.makeText(context, R.string.toast_6, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    /**
     * disconnect bluethooth
     */
    public static void disconnectBlueTooth() {
        if (bluetoothSocket != null) {
            try {
                OutputStream out = bluetoothSocket.getOutputStream();
                out.close();
                bluetoothSocket.close();
                bluetoothSocket = null;
                isBlueToothPrinter = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  send esc cmd
     */
    public static void sendData(byte[] bytes) {
        if (bluetoothSocket != null) {
            OutputStream out = null;
            try {
                out = bluetoothSocket.getOutputStream();
                out.write(bytes, 0, bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            //TODO handle disconnect event
            Log.d("aaa,","socket null");
        }
    }

}
