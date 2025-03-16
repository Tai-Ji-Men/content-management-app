package com.tjm.common;

import com.tjm.configmanager.ConfigManager;
import com.tjm.configmanager.TJMConfigManagerFactory;

public class GlobalContextLoader {
    public static void loadContext() {
        ConfigManager configManager = TJMConfigManagerFactory.getConfigManager();
        System.out.println("configManager loaded!!!");
        // load db connections
        TJMMongoConnection.connect();
        // setup config manager
    }
}
