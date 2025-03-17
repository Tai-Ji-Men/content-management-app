package com.tjm.common;

import com.tjm.common.exceptions.MongoConnectionException;
import com.tjm.common.mongo.TJMMongoConnection;
import com.tjm.configmanager.ConfigManager;
import com.tjm.configmanager.TJMConfigManagerFactory;

public class GlobalContextLoader {
    public static void loadContext() {
        ConfigManager configManager = TJMConfigManagerFactory.getConfigManager();
        System.out.println("configManager loaded!!!");
        // test
        System.out.println("test.key = " + configManager.getPropertyString("test.key", ""));
        // load db connections
        try {
            TJMMongoConnection.connect();
        } catch (MongoConnectionException e) {
            throw new RuntimeException(e);
        }
        // setup config manager
    }
}
