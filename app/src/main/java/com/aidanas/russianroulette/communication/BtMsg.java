package com.aidanas.russianroulette.communication;

import java.io.Serializable;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 21/04/2016.
 *
 * Class to model a single inter device Bluetooth message.
 */
public class BtMsg implements Serializable{

    public static final int MASTER_NOT_LISTENING = 20;
    public static final int CONNECTED_MASTER_SOCKET = 22;

    public static final int SLAVE_ACK = 10;
    public static final int SLAVE_CONNECTION_FAIL = 11;
    public static final int CONNECTED_SLAVE_SOCKET = 12;

    public static final int BT_MESSAGE_READ = 30;

    // Server To Client message types.
    public static final int STC_NEW_PLAYER   = 501;
    public static final int STC_PLAYERS_LIST = 502;
    public static final int STC_SERVER_READY = 503;

    // Client To Server message types.
    public static final int CTS_CLIENT_READY = 603;

    // Contents of a message passed between players.
    public int type;
    public Object payload; // Must be cast to the expected object type upon reception.

    public String srcMAC;

}
