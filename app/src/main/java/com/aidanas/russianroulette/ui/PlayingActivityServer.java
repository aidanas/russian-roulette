package com.aidanas.russianroulette.ui;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.R;
import com.aidanas.russianroulette.adapters.PlayersListArrayAdapter;
import com.aidanas.russianroulette.communication.BtMsg;
import com.aidanas.russianroulette.services.GameService;

import java.util.ArrayList;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 02-05-2016
 *
 * Class to contain the "Playing" screen of the game.
 * NOTE: Server side.
 */
public class PlayingActivityServer extends Activity {

    // Tag, mostly used for logging and debug output.
    public static final String TAG = PlayingActivityServer.class.getSimpleName();

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

    // Views
    private ListView mPlayersLw;
    private Button mReadyBtn;

    // List adapter
    private PlayersListArrayAdapter mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Const.DEBUG) Log.v(TAG, "In onCreate()");

        setContentView(R.layout.activity_playing);

        mReadyBtn  = (Button) findViewById(R.id.ac_playing_ready_btn);


        mReadyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Const.DEBUG) Log.v(TAG, "In onClick()");

                // TODO: 02/05/2016 Implement this!

            }
        });

        mPlayersLw = (ListView) findViewById(R.id.ac_playing_players_lw);
        mArrayAdapter = new PlayersListArrayAdapter(this, R.layout.player_list_item,
                new ArrayList<BluetoothDevice>());
        mPlayersLw.setAdapter(mArrayAdapter);

        startGameService(true);
        bindToService(GameService.class, mGameServiceConnection = new GameServiceConnection());
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

    /***********************************************************************************************
     *                                  Inner Classes
     **********************************************************************************************/

    private class MainHandler extends Handler {

        // Tag, mostly used for logging and debug output.
        public final String TAG = MainHandler.class.getSimpleName();

        public MainHandler(Looper mainLooper) {
            super(mainLooper);
        }


        @Override
        public void handleMessage(Message msg) {
            if (Const.DEBUG) Log.v(TAG, "in handleMessage(), msg.obj = " + msg.obj + "Thread = " +
                    Thread.currentThread().getName());

            /*
             * Main switching block.
             */
            switch (msg.what) {
                case BtMsg.BT_MESSAGE_READ:
                    // TODO: 02/05/2016 handle msg!
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
