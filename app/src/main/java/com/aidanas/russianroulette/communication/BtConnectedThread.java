package com.aidanas.russianroulette.communication;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.aidanas.russianroulette.Const;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by: Aidanas
 * Created on: 22/04/2016.
 *
 * Class to holds the logic for reading and writing into connected bluetooth sockets.
 */
public class BtConnectedThread extends Thread{

    // Tag, mostly used for logging and debug output.
    public static final String TAG = BtConnectedThread.class.getSimpleName();

    // Size of the input buffer in bytes.
    private static final int BUFFER_SIZE = 1024;

    private final InputStream mInputStream;
    private final OutputStream mOutputStream;
    private final BluetoothSocket mBluetoothSocket;

    private final Messenger mMessenger;

    /**
     * Constructor.
     * @param bluetoothSocket - A connected Bluetooth socket through which the communication will be
     *                        going.
     * @param messenger - Messenger to which received data will be passed.
     */
    public BtConnectedThread(BluetoothSocket bluetoothSocket, Messenger messenger)
            throws IOException {
        mInputStream = bluetoothSocket.getInputStream();
        mOutputStream = bluetoothSocket.getOutputStream();
        mBluetoothSocket = bluetoothSocket;
        mMessenger = messenger;
    }

    /**
     * Thread's starting point.
     */
    public void run() {
        if (Const.DEBUG) Log.v(TAG, "In run(), Thread = " + Thread.currentThread().getName());

        // Buffer to store data from input stream and the data read counter.
        byte[] inBuffer = new byte[BUFFER_SIZE];
        int bytesRead;


        while (true) {
            try {
                if (Const.DEBUG) Log.v(TAG, "In run(), reading " +
                        mBluetoothSocket.getRemoteDevice().getAddress());

                bytesRead = mInputStream.read(inBuffer);

                // TODO: 22/04/2016 Remove after DEBUGGING is completed!
                if (Const.DEBUG) Log.v(TAG, "In run(), mInputStream.read() returned with " +
                        bytesRead + " bytes read.");

                passToMessenger(BtMsg.BT_MESSAGE_READ, bytesRead, -1, inBuffer);
            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        if (Const.DEBUG) Log.v(TAG, "In write(), writing to: " +
                mBluetoothSocket.getRemoteDevice().getAddress() +
                ", bytes = " + Arrays.toString(bytes));

        try {
            mOutputStream.write(bytes);
            mOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mBluetoothSocket.close();
        } catch (IOException e) { }
    }

    /**
     * Method to construct a message form the passed parameters and post them to the messenger.
     * @param what - Message type.
     * @param arg1 - Argument for message arg1 field.
     * @param arg2 - Argument for message arg2 field.
     * @param obj  - Object to be passed to the main thread.
     */
    private void passToMessenger(int what, int arg1, int arg2, Object obj) {
        if (Const.DEBUG) Log.v(TAG, "In passToMessenger(), dispatching msg.what = " + what +
                " , obj = " + new String((byte[]) obj));

        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;

        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
