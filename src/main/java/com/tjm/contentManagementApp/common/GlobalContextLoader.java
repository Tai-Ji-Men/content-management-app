package com.tjm.contentManagementApp.common;

public class GlobalContextLoader {
    public static void loadContext() {
        // load db connections
        TJMMongoConnection.connect();
        // some other setup before running the application
    }
}
