package com.aidanas.russianroulette.game;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.communication.BtConnectedThread;
import com.aidanas.russianroulette.communication.BtMasterThread;
import com.aidanas.russianroulette.communication.BtMsg;
import com.aidanas.russianroulette.interfaces.BluetoothSocketReceiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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

    // Incoming messages will  be processed by thread kept in this list.
    private final List<BtConnectedThread> mConnectedThreads = new ArrayList<>();

    // Players currently in the game.
    private final List<Player> playersList = new ArrayList<>();

    /*
     * Looper thread and its handler. This will provide a separate thread which the communication
     * threads can use to post received messages.
     */
    private final HandlerThread mHandlerThread;
    private final Handler mHandler;

    /**
     * Constructor.
     * @param isServer - Is the device running as the server of the game?
     */
    public Arbitrator(boolean isServer, Messenger messenger){
        mIsServer = isServer;
        mMessenger = messenger;

        // Setup the looper for JobCoordinator. It is going to be the main thread coordinating work.
        mHandlerThread = new HandlerThread("JC Handler Thread");
        mHandlerThread.start();
        mHandler = new ArbitratorHandler(mHandlerThread.getLooper()); // TODO: Disable on termination
    }

    /**
     * Method to process a newly come device.
     * @param bluetoothSocket - Connected socket to the new device.
     */
    private void newPlayer(BluetoothSocket bluetoothSocket) {
        if (Const.DEBUG) Log.v(TAG, "In newPlayer(), bluetoothSocket" + bluetoothSocket);

        // If running as the host then update UI and inform other clients.
        if (mIsServer){
            mConnectedSockets.add(bluetoothSocket);
            Player p = playersFromSocket(bluetoothSocket);
            playersList.add(p);
            updateUIPlayerList();
            notifyClientsNewPlayer(p);
        }
    }

    /**
     * Utility method to instantiate and initialise a player object from a given bluetooth socket.
     * @param bluetoothSocket - Conected Bluetooth socket.
     * @return - Player object.
     */
    private Player playersFromSocket(BluetoothSocket bluetoothSocket) {
        if (Const.DEBUG) Log.v(TAG, "In playersFromSocket()");

        return new Player(bluetoothSocket.getRemoteDevice().getName(),
                bluetoothSocket.getRemoteDevice().getAddress());
    }

    /**
     * Method to inform all clients that a new player has arrived.
     * @param player - New player.
     */
    private void notifyClientsNewPlayer(Player player) {
        if (Const.DEBUG) Log.v(TAG, "In notifyClientsNewPlayer(), player = " + player.getName());

        for (BtConnectedThread t: mConnectedThreads) {
            String addr = t.getBluetoothSocket().getRemoteDevice().getAddress();
            if (!addr.equals(player.getAddress())){
                BtMsg btmsg = new BtMsg();
                btmsg.type = BtMsg.STC_NEW_PLAYER;
                btmsg.payload = player;
                t.write(btmsg);
            }
        }
    }

    /**
     * Method to update player list on the UI.
     */
    private void updateUIPlayerList() {
        if (Const.DEBUG) Log.v(TAG, "In updateUIPlayerList()");

        Message m = Message.obtain();
        m.what    = UPDATE_PLAYER_LIST;
        m.obj     = playersList;
        try {
            mMessenger.send(m);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to return bytes of en object passed as a parameter to it.
     * @param obj - Object to returned as an array of bytes.
     * @return - Byte array.
     */
    private byte[] objTobyteArr(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    /**
     * MMethod to construct an object out of bytes provided in the array supplied as the argument
     * @param arr - Array of bytes from which the object will be read.
     * @return - Object read from the byte array.
     */
    private Object byteArrToObj(byte[] arr) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(arr);
        ObjectInput in = new ObjectInputStream(bis);
        Object obj = null;
        try {
            obj = in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
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

        // For every connected socket spawn a new thread for reading data.
        try {
            BtConnectedThread t = new BtConnectedThread(bluetoothSocket, mHandler);
            t.start();
            mConnectedThreads.add(t);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * Server should inform others about the new player. Client just adds the socket as a
         * player.
         */
        if (mIsServer){
            newPlayer(bluetoothSocket);
        } else {
            playersList.add(playersFromSocket(bluetoothSocket));
            updateUIPlayerList();
        }
    }

    /***********************************************************************************************
     *                                  Inner Classes
     **********************************************************************************************/

    private class ArbitratorHandler extends Handler {

        // Tag, mostly used for logging output.
        public final String TAG = ArbitratorHandler.class.getSimpleName();

        /**
         * Constructor simply delegates the looper to its superclass.
         * @param looper - Message queue.
         */
        public ArbitratorHandler(Looper looper){
            super(looper);
        }

        /**
         * Method processes the messages posted onto this handler.
         * @param inputMessage - Message received. Fields of interest : 'what' and 'obj'.
         */
        @Override
        public void handleMessage(Message inputMessage){
            if (Const.DEBUG) Log.v(TAG, "In handleMessage(), msg.what = " + inputMessage.what +
                    "msg.obj = " + inputMessage.obj + ", Thread = " +
                    Thread.currentThread().getName());

            // Switch on the type of the BtMsg.
            switch (inputMessage.what){

                case BtMsg.STC_NEW_PLAYER:
                    playersList.add((Player)inputMessage.obj);
                    updateUIPlayerList();
                    break;

                default:
                    super.handleMessage(inputMessage);
            }
        }
    }

}
