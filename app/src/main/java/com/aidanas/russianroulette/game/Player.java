package com.aidanas.russianroulette.game;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by: Aidanas
 * Created on: 22/04/2016.
 *
 * Class to model a single player in the game. It will be used by the Arbitrator.
 */
public class Player implements Serializable{

    // Must be unique among players.
    private final String mName;

    private final String mAddress;

    private boolean mIsReady = false;

    /**
     * Constructor
     * @param name - Name of the player.
     */
    public Player(String name, String address){
        mName = name;
        mAddress = address;
    }

    /***********************************************************************************************
     *                          Getters and Setters
     **********************************************************************************************/

    public String getName() {
        return mName;
    }

    public String getAddress(){
        return mAddress;
    }

    public boolean isReady(){
        return mIsReady;
    }

    public void setReady(boolean isReady){
        mIsReady = isReady;
    }

    /***********************************************************************************************
     *                          Inner Classes
     **********************************************************************************************/

    /**
     * Custom comparator class to compare two Players in terms of their names.
     */
    public class PlayerComparator implements Comparator<Player>{

        @Override
        public int compare(Player lhs, Player rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}
