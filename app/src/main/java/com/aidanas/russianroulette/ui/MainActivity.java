package com.aidanas.russianroulette.ui;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aidanas.russianroulette.communication.BtMasterThread;
import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.R;

import java.util.UUID;

/**
 * Class containing the main activity of the app.
 */
public class MainActivity extends AppCompatActivity {

    // Tag, mostly used for logging and debug output.
    public static final String TAG = BtMasterThread.class.getSimpleName();

    // Custom handler to handle messages coming from other threads.
    private final Handler mHandler = new MainHandler(Looper.getMainLooper());

    // Views of the activity.
    private TextView mTextView;
    private Button mServerBtn;
    private Button mClientBtn;
    private Button mSendBtn;

    // Bluetooth adapter which will be used for communication.
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BtMasterThread mBtMasterThread;

    // Server device flag.
    private Boolean isServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mTextView  = (TextView) findViewById(R.id.ac_main_tv);
        mServerBtn = (Button) findViewById(R.id.ac_main_server_btn);
        mClientBtn = (Button) findViewById(R.id.ac_main_client_btn);
        mSendBtn   = (Button) findViewById(R.id.ac_main_send_btn);

        /*
         * Check if Bluetooth is supported by the device.
         */
        if (mBluetoothAdapter == null) {
            // TODO: Exit. Device does not support bluetooth
        } else {

        }

        View.OnClickListener clickListener;
        mServerBtn.setOnClickListener(clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == mServerBtn){
                    if (Const.DEBUG) Log.v(TAG, "In onClick(), *Server Btn.*");
                    startBtServer();
                } else {
                    if (Const.DEBUG) Log.v(TAG, "In onClick(), *Client Btn.*");
                    startBtClient();
                }
            }
        });

        mClientBtn.setOnClickListener(clickListener);

    }

    private void startBtServer() {
        enableBtConBtns(false);
        isServer = true;
        mBtMasterThread = new BtMasterThread(getString(R.string.app_name),
                UUID.fromString(getString(R.string.UUID)), mHandler);
        mBtMasterThread.start();
    }

    private void startBtClient() {
        enableBtConBtns(false);
        isServer = false;
    }

    /**
     * Utility method to enable/disable UI Bluetooth Server/Client buttons.
     * @param b - True to enable, false otherwise.
     */
    private void enableBtConBtns(boolean b) {
        mServerBtn.setEnabled(b);
        mClientBtn.setEnabled(b);
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    /***********************************************************************************************
     *                        Only Android Lifecycle Methods Above
     **********************************************************************************************/

    private class MainHandler extends Handler{
        // Tag, mostly used for logging and debug output.
        public final String TAG = MainHandler.class.getSimpleName();

        public MainHandler(Looper mainLooper) {
            super(mainLooper);
        }


        @Override
        public void handleMessage(Message msg) {
            if (Const.DEBUG) Log.v(TAG, "in handleMessage(), msg.obj = " + msg.obj + "Thread = " +
                    Thread.currentThread().getName());
            super.handleMessage(msg);
        }
    }
}
