package com.aidanas.russianroulette.game;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.communication.BtMasterThread;
import com.aidanas.russianroulette.interfaces.BluetoothSocketReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by: Aidanas
 * Created on: 21/04/2016.
 * <p>
 * Class to contain the Russian Roulette game logic.
 */
public class Arbitrator implements BluetoothSocketReceiver{

    // Tag, mostly used for logging and debug output.
    public static final String TAG = BtMasterThread.class.getSimpleName();

    // Holds a list of connected sockets. Only server would contains more than one item in it.
    private final List<BluetoothSocket> mConnectedSockets =
            Collections.synchronizedList(new ArrayList<BluetoothSocket>());

    /**
     * Callback method which is called when connection is established providing a connected
     * Bluetooth socket as an argument.
     * @param bluetoothSocket
     */
    @Override
    public synchronized void receiveSocket(BluetoothSocket bluetoothSocket) {
        if (Const.DEBUG) Log.v(TAG, "In receiveSocket(), Adding connected socket to:" +
                bluetoothSocket.getRemoteDevice().getAddress() + ", Thread = " +
                Thread.currentThread().getName());

        mConnectedSockets.add(bluetoothSocket);
    }
}
