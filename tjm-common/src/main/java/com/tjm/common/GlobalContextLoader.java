package com.tjm.common;

public class GlobalContextLoader {
    public static void loadContext() {
        // load db connections
        TJMMongoConnection.connect();
        // setup config manager
        //


    }
}
