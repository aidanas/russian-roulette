package com.aidanas.russianroulette.ui;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.R;
import com.aidanas.russianroulette.adapters.PlayersListArrayAdapter;
import com.aidanas.russianroulette.game.Arbitrator;
import com.aidanas.russianroulette.game.Player;
import com.aidanas.russianroulette.services.GameService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 02-05-2016
 *
 * Class to contain the "Playing" screen of the game.
 * NOTE: Client side.
 */
public class PlayingActivityClient extends Activity {

    // Tag, mostly used for logging and debug output.
    public static final String TAG = PlayingActivityClient.class.getSimpleName();

    // Key to access a Intent extras passed to the game service.
    public static final String MESSENGER = "messenger";
    public static final String IS_SERVER = "start as server?";

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
    private Button mAnotherBtn;
    private ImageView mGuyIv;
    private TextView mTitleTv;

    // List adapter
    private PlayersListArrayAdapter mArrayAdapter;

    // Address of the host device.
    private String mastersMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Const.DEBUG) Log.v(TAG, "In onCreate()");

        setContentView(R.layout.activity_playing);

        // Get the mac address of the hosting device.
        mastersMac = getIntent().getExtras().getString(SelectHostActivity.HOST_MAC_ADDR);

        mTitleTv    = (TextView) findViewById(R.id.ac_playing_title_tv);
        mReadyBtn   = (Button) findViewById(R.id.ac_playing_ready_btn);
        mAnotherBtn = (Button) findViewById(R.id.ac_playing_another_btn);
        mGuyIv      = (ImageView) findViewById(R.id.ac_playing_guy_iv);

        /*
         * When "I'm Ready" is pressed mark the players as ready. This involves notifying the
         * service so it can take appropriate action depending on whether the device is a host or
         * not.
         */
        mReadyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Const.DEBUG) Log.v(TAG+"[ANON]", "In onClick()");

                // Hide the button and display the image...
                mReadyBtn.setVisibility(View.GONE);
                mGuyIv.setVisibility(View.VISIBLE);
                mTitleTv.setText(getString(R.string.waiting_for_others));
                mGameService.readyUp();
            }
        });

        /*
         * This button will only be shown if the players survives a round. It allows the user to
         * request another round of the game.
         */
        mAnotherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Const.DEBUG) Log.v(TAG+"[ANON]", "In onClick() mAnotherBtn.");

                // TODO: 06/05/2016 to be specified.
            }
        });

        // Configure the list displaying players.
        mPlayersLw = (ListView) findViewById(R.id.ac_playing_players_lw);
        mArrayAdapter = new PlayersListArrayAdapter(this, R.layout.player_list_item,
                new ArrayList<Player>());
        mPlayersLw.setAdapter(mArrayAdapter);

        startGameService(false);
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

        // If running as client our service will need maters mac address.
        if (!isServer){
            intent.putExtra(SelectHostActivity.HOST_MAC_ADDR, mastersMac);
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
            if (Const.DEBUG) Log.v(TAG, "In handleMessage(), msg.what = " + msg.what +
                    ", Thread = " + Thread.currentThread().getName());

            /*
             * Main switching block.
             */
            switch (msg.what) {

                case Arbitrator.MSG_UI_UPDATE_PLAYER_LIST:
                    updatePlayerList((List<Player>) msg.obj);
                    break;

                case Arbitrator.MSG_UI_ALL_READY:
                    mTitleTv.setText(R.string.playing);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Method to update the list of players on UI with the newly provided list.
     * @param players - List of players.
     */
    private void updatePlayerList(List<Player> players) {
        if (Const.DEBUG) Log.v(TAG, "In updatePlayerList(), players.size() = " + players.size() +
            ", Thread = " + Thread.currentThread().getName() );

        mArrayAdapter.clear();
        mArrayAdapter.addAll(players);
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

            // Extract and save the reference to the service. Enable 'I'm Ready' button.
            GameService.ServiceBinder binder = (GameService.ServiceBinder) service;
            mGameService = binder.getGameService();
            mBound = true;
            mReadyBtn.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            if (Const.DEBUG) Log.v(TAG, "In onServiceDisconnected()");
            mBound = false;
        }
    }
}
