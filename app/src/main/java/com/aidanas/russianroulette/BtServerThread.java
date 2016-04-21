package com.aidanas.russianroulette;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 21/04/2016.
 */
public class BtServerThread extends Thread{

    // Tag, mostly used for logging and debug output.
    public static final String TAG = BtServerThread.class.getSimpleName();

    private final BluetoothServerSocket mServerSocket;

    // Used to pass messages back to the main thread.
    private final Handler mHandler;


    /**
     * Constructor.
     * @param bluetoothAdapter - Adapter which will be used to acquire connected sockets.
     */
    public BtServerThread(BluetoothAdapter bluetoothAdapter, String name, UUID uuid,
                          Handler handler) {

        // Open a Bluetooth server socket to listen for incoming connections.
        BluetoothServerSocket tmpSoc = null;
        try {
            tmpSoc = bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid);
        } catch (IOException e) {

        }
        mServerSocket = tmpSoc;
        mHandler = handler;
    }

    public void run() {
        BluetoothSocket btSoc = null;

        /*
         * Accept as many connections as we can. Passing them to the arbitrator.
         */
        while (true) {
            try {
                btSoc = mServerSocket.accept();
            } catch (IOException e) {
                // Caused as well by cancel() method call.
                break;
            }
            // If a connection was accepted
            if (btSoc != null) {
                // Do work to manage the connection (in a separate thread)
//                manageConnectedSocket(socket);
            }
        }
    }

    /**
     * Closes the server listening socket causing the thread to finish.
     */
    public void cancel() {
        try {
            mServerSocket.close();
        } catch (IOException e) { }
    }

}
