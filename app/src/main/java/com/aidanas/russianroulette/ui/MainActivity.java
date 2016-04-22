package com.aidanas.russianroulette.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.R;
import com.aidanas.russianroulette.communication.BtMsg;
import com.aidanas.russianroulette.services.GameService;

/**
 * Class containing the main activity of the app.
 */
public class MainActivity extends AppCompatActivity{

    // Tag, mostly used for logging and debug output.
    public static final String TAG = MainActivity.class.getSimpleName();

    // Key to access a Intent extras passed to the game service.
    public static final String MESSENGER = "messenger";
    public static final String IS_SERVER = "start as server?";
    public static final String MASTERS_MAC = "mac address of the master";

    // Custom handler to handle messages coming from other threads.
    private final Handler mHandler = new MainHandler(Looper.getMainLooper());

    // Messenger to be passed to server service. It will deliver messages to the handler.
    private final Messenger mMessenger = new Messenger(mHandler);

    // Service which will contain the game and Bluetooth communication logic.
    private GameService mGameService;
    private ServiceConnection mGameServiceConnection;

    // Is the activity currently bound to the above service?
    private boolean mBound = false;

    // Views of the activity.
    private TextView mTextView;
    private EditText mEditText;
    private Button mServerBtn;
    private Button mClientBtn;
    private Button mSendBtn;

    // Bluetooth adapter which will be used for communication.
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mTextView  = (TextView) findViewById(R.id.ac_main_title_tv);
        mEditText  = (EditText) findViewById(R.id.ac_main_et);
        mServerBtn = (Button) findViewById(R.id.ac_main_server_btn);
        mClientBtn = (Button) findViewById(R.id.ac_main_client_btn);
        mSendBtn   = (Button) findViewById(R.id.ac_main_send_btn);

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Const.DEBUG) Log.v(TAG, "In onClick()");

                if (mGameService != null){
                    mGameService.sendMsg(mEditText.getText().toString());
                }
            }
        });

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
                if (Const.DEBUG) Log.v(TAG, "In onClick(), Server Btn? = " + (v == mServerBtn));

                enableBtConBtns(false);
                startGameService(v == mServerBtn);
            }
        });

        mClientBtn.setOnClickListener(clickListener);
        bindToService(GameService.class, new GameServiceConnection());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Const.DEBUG) Log.v(TAG, "In onStart()");
    }

    /***********************************************************************************************
     *                        Only Android Lifecycle Methods Above
     **********************************************************************************************/

    /**
     * Method to bind to a service.
     */
    private void bindToService(Class<?> service, ServiceConnection serviceConnection){
        if (Const.DEBUG) Log.v(TAG, "In bindToService(), req bind to:" + service.getSimpleName());

        bindService(new Intent(this, service),  mGameServiceConnection = serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * Method to start the GameService service.
     * @param isServer - Start as a server device?
     */
    private void startGameService(boolean isServer) {
        if (Const.DEBUG) Log.v(TAG, "In startGameService()");

        // Supply the service with a messenger so it can pass messages back to this activity.
        Intent intent = new Intent(this, GameService.class);
        intent.putExtra(MESSENGER, mMessenger);
        intent.putExtra(IS_SERVER, isServer);

        // TODO: 22/04/2016 Remove after DEBUGGING is completed! HARDCODED MAC!
        if (!isServer){
            intent.putExtra(MASTERS_MAC, "BC:CF:CC:F9:8C:9C");
        }

        startService(intent);
    }

    /**
     * Utility method to enable/disable UI Bluetooth Server/Client buttons.
     * @param b - True to enable, false otherwise.
     */
    private void enableBtConBtns(boolean b) {
        mServerBtn.setEnabled(b);
        mClientBtn.setEnabled(b);
    }

    /***********************************************************************************************
     *                                  Inner Classes
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

            // TODO: 22/04/2016 Remove after DEBUGGING is completed! SHOULD it be moved to the service?
            /*
             * Main switching block.
             */
            switch (msg.what) {
                case BtMsg.BT_MESSAGE_READ:
                    mTextView.setText(new String((byte[])msg.obj));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Class implementing ServiceConnection interface. It provides with a set of callback methods
     * which are called as a consequence of a bindService() call. This class ensures that this
     * activity obtains a reference to the service so it can communicate with it calling its public
     * methods
     */
    public class GameServiceConnection implements ServiceConnection {

        // Tag, mostly used for logging and debug output.
        public final String TAG = GameServiceConnection.class.getSimpleName();

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (Const.DEBUG) Log.v(TAG, "In onServiceConnected(), className = " + className);

            GameService.ServiceBinder binder = (GameService.ServiceBinder) service;
            mGameService = binder.getGameService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            if (Const.DEBUG) Log.v(TAG, "In onServiceDisconnected()");

            mBound = false;
        }
    }
}
