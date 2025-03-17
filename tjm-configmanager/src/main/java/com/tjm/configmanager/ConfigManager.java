package com.tjm.configmanager;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;

import java.util.*;

public class ConfigManager {
    private CombinedConfiguration configuration;
    private DatabaseConfiguration databaseConfiguration;
    private List<String> priorities = Arrays.asList("propertiesFromDB", "staticConfiguration", "databaseConfiguration");
    private Map<String, CombinedConfiguration> configMap;
    private CombinedConfiguration config;

    public ConfigManager(CombinedConfiguration config) throws Exception {
        if (config == null || config.isEmpty()) {
            throw new Exception("Cannot create Config Manager without a Configuration");
        }
        this.config = config;
//        logger.debug("Created config manager for: " + getContextName());
//        buildConfig();
    }

//    protected void buildConfig() {
////        logger.info("Populating config manager for: " + getContextName());
//        configuration = new CombinedConfiguration();
//        configuration.clear();
//        configuration.setNodeCombiner(new OverrideCombiner());
////        configuration.addConfigurationListener(new ReconConfigurationListener());
//        OrderedProperties orderProperties=this.getOrderedSubset("configorder");
//        Set<String> orderedSet=null;
//        if(orderProperties!=null && orderProperties.size()!=0){
////            logger.info(">>>ordered properties is not empty>>>>>");
//            orderedSet=new LinkedHashSet(orderProperties.keySet());
//            priorities.clear();
////            logger.debug(">>>already populated list got cleared to be overriden>>>>");
//            Iterator<String> priorityOrder=orderedSet.iterator();
//            while(priorityOrder.hasNext()) {	String nextKeyValue=priorityOrder.next();
//                if(nextKeyValue!=null && nextKeyValue.trim().length()!=0){
//                    priorities.add(orderProperties.getProperty(nextKeyValue));
////                    logger.info(">>>>added configuration is:::"+nextKeyValue);
//                }
//            }
//        }
//        Iterator<String> itPriorities = this.priorities.iterator();
//        while (itPriorities.hasNext()) {
//            String priority = itPriorities.next();
//            if (configMap.containsKey(priority)) {
//                configuration.addConfiguration(configMap.get(priority),priority);
////                logger.info("Updated config manager for " + priority);
//            }
//        }
//        if (databaseConfiguration != null) {
//            configuration.removeConfiguration(databaseConfiguration);
//            configuration.addConfiguration(databaseConfiguration,"databaseConfiguration");
//            logger.info("Updated config manager for databaseConfiguration");
//        }
//        //rdar://82262535: Avoid using this function as it will likely print sensitive information to the logs
//        //printConfiguration();
//    }

    public String getPropertyString(String propertyName, String defaultValue) {

        String value = getConfiguration().getString(propertyName);

        if (value == null) {
            value = defaultValue;
        }

        return value;
    }

    private Configuration getConfiguration() {
        return this.config;
    }


}
