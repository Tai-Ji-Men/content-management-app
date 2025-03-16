package com.tjm.configmanager;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.DatabaseConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private CombinedConfiguration configuration;
    private DatabaseConfiguration databaseConfiguration;

    private List<String> priorities = Arrays.asList("propertiesFromDB", "staticConfiguration", "databaseConfiguration");
    private Map<String, CombinedConfiguration> configMap;
    

}
