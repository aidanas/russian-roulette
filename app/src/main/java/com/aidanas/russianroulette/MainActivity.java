package com.aidanas.russianroulette;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.UUID;

/**
 * Class containing the main activity of the app.
 */
public class MainActivity extends AppCompatActivity {

    // Tag, mostly used for logging and debug output.
    public static final String TAG = BtServerThread.class.getSimpleName();

    private static BtServerThread mBtServerThread;

    // Views of the activity.
    private static TextView mTextView;


    // Bluetooth adapter which will be used for communication.
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.ac_main_tv);


        if (mBluetoothAdapter == null) {
            // TODO: Exit. Device does not support bluetooth
        } else {
            mBtServerThread = new BtServerThread(mBluetoothAdapter, getString(R.string.app_name),
                    UUID.fromString(getString(R.string.UUID)));
            mBtServerThread.start();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();


    }
}
