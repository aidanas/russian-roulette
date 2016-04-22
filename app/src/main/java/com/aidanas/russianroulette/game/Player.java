package com.aidanas.russianroulette.game;

import java.util.Comparator;

/**
 * Created by: Aidanas
 * Created on: 22/04/2016.
 *
 * Class to model a single player in the game. It will be used by the Arbitrator.
 */
public class Player {

    // Must be unique among players.
    private final String mName;

    /**
     * Constructor
     * @param name - Name of the player.
     */
    public Player(String name){
        mName = name;

    }

    /***********************************************************************************************
     *                          Getters and Setters
     **********************************************************************************************/

    public String getName() {
        return mName;
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
            lhs.getName().compareTo(rhs.getName());
        }
    }
}
