package com.aidanas.russianroulette.game;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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
 *
 * Class to contain the Russian Roulette game logic.
 */
public class Arbitrator implements BluetoothSocketReceiver{

    // Tag, mostly used for logging and debug output.
    public static final String TAG = BtMasterThread.class.getSimpleName();

    public static final int UPDATE_PLAYER_LIST = 40;

    // Flag to be set for the device acting as a server of the game.
    private final boolean mIsServer;

    // To communicate with activity.
    private final Messenger mMessenger;

    // Holds a list of connected sockets. Only server would contains more than one item in it.
    private final List<BluetoothSocket> mConnectedSockets =
            Collections.synchronizedList(new ArrayList<BluetoothSocket>());

    private final List<Player> playersList = new ArrayList<>();

    /**
     * Constructor.
     * @param isServer - Is the device running as the server of the game?
     */
    public Arbitrator(boolean isServer, Messenger messenger){
        mIsServer = isServer;
        mMessenger = messenger;
    }

    /**
     * Method to process a newly come device.
     * @param bluetoothSocket - Connected socket to the new device.
     */
    private void newPlayer(BluetoothSocket bluetoothSocket) {
        if (Const.DEBUG) Log.v(TAG, "In newPlayer(), bluetoothSocket" + bluetoothSocket);

        if (mIsServer){
            mConnectedSockets.add(bluetoothSocket);
            playersList.add(new Player(bluetoothSocket.getRemoteDevice().getName()));
            updatedPlayerList();
        }
    }

    /**
     * Method to update player list on the UI.
     */
    private void updatedPlayerList() {
        if (Const.DEBUG) Log.v(TAG, "In updatedPlayerList()");

        Message m = Message.obtain();
        m.what    = UPDATE_PLAYER_LIST;
        m.obj     = mConnectedSockets;
        try {
            mMessenger.send(m);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /***********************************************************************************************
     *                                  Interface Implementations
     **********************************************************************************************/

    /**
     * Callback method which is called when connection is established providing a connected
     * Bluetooth socket as an argument.
     * @param bluetoothSocket - Connected Bluetooth socket.
     */
    @Override
    public synchronized void receiveSocket(BluetoothSocket bluetoothSocket) {
        if (Const.DEBUG) Log.v(TAG, "In receiveSocket(), Adding connected socket to:" +
                bluetoothSocket.getRemoteDevice().getAddress() + ", Thread = " +
                Thread.currentThread().getName());


        newPlayer(bluetoothSocket);
    }
}
