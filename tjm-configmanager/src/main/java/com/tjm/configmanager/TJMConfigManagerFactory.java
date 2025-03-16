package com.tjm.configmanager;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TJMConfigManagerFactory {
    public static final HashMap<String, ConfigManager> configMap = new HashMap<String, ConfigManager>();
    private static final String[] FILE_FILTER_EXTENSION = new String[]{"properties"};
    public static ConfigManager config = null;

    private static final CombinedConfiguration propFromDBConfiguration = new CombinedConfiguration();

    private static final String EMPTY_STRING="";
    private static final String REPLACE_FILE="file:";

    private static final String DOT_CONST="#dot#";

    private static volatile String serverRealPath;

    private static AtomicBoolean isMongoDBPropertiesReady = new AtomicBoolean(false);

    public static ConfigManager getConfigManager() {
        if (config == null) {
            synchronized (TJMConfigManagerFactory.class) {
                if (config == null) {
                    initConfiguration();
                }
            }
        }
        return config;
    }

    private static void initConfiguration() {
//        logger.info("inside initconfiguration method for context  "
//                + contextName);
        try {
            CombinedConfiguration staticConfiguration = new CombinedConfiguration();
            staticConfiguration.setNodeCombiner(new OverrideCombiner());
            loadAllProperties(staticConfiguration);

            HashMap<String, CombinedConfiguration> configurationMap = new HashMap<String, CombinedConfiguration>();

            configurationMap.put("staticConfiguration", staticConfiguration);

            propFromDBConfiguration.setNodeCombiner(new OverrideCombiner());
            configurationMap.put("propertiesFromDB", propFromDBConfiguration);

            ConfigManager configManager = new ConfigManager(configurationMap);
//            configManager.setContextName(contextName);
//            configMap.put(contextName, configManager);
            //below block commented out during Property consolidation activity. Reload folder was removed as part of activity making the below block redundant
            /**ScheduledExecutorService executor = ScheduledExecutor.getExecutor();
             FileChangeNotifier fileChangeNotifier = new FileChangeNotifier();
             fileChangeNotifier.initializeConfiguration(contextName);
             executor.execute(fileChangeNotifier);
             updated Temporarily the value from 0 to 10800 to fix the NullPointer issue in some report jobs
             executor.scheduleAtFixedRate(fileChangeNotifier, 10800, 10, TimeUnit.SECONDS);
             logger.info("<<file change notifier terminated>>  " + executor.isTerminated());**/

        } catch (Exception exc) {

            //Catch all the Exceptions and throw RunTime exception from here
//            logger.error(
//                    "Exception occurred in configuring the configmanager for context  "
//                            + contextName, exc);
//            ReconConfigManagerException reconException = new ReconConfigManagerException(exc);
//            reconException.setContext(contextName);
//            reconException.setFatal(true);
//            reconException.setModule("Configuration Manager");
            throw new RuntimeException("exception happened in TJMConfigManagerFactory");
        }
    }


    private static void loadAllProperties(
            CombinedConfiguration combinedConfiguration)
            throws Exception {
        // calculate the path
        String tjmCoreAppParentPath = System.getProperty("user.dir");
        iterateDirectory(combinedConfiguration, tjmCoreAppParentPath);
    }

    private static synchronized void iterateDirectory(
            CombinedConfiguration combinedConfiguration, String propertiesPath)
            throws IOException, ConfigurationException {

        File f = new File(propertiesPath);

        List<File> files = (List<File>) FileUtils.listFiles(f, FILE_FILTER_EXTENSION, Boolean.TRUE);
        for (File file : files) {
            if (isUnderTarget(file)) {
                // do nothing. ignore
                System.out.println("the path ignored when constructing configManager: " + file.getCanonicalPath());
            } else {
                PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
                /**This will ensure that all properties are treated as String.The Delimiter parsing will be disabled**/
                propertiesConfiguration.setDelimiterParsingDisabled(true);
                /**end**/
                propertiesConfiguration.load(file.getCanonicalPath());
                String configFileName = FilenameUtils.removeExtension(file.getName());
                combinedConfiguration.addConfiguration(propertiesConfiguration, configFileName);
            }
        }
    }

    public static boolean isUnderTarget(File file) throws IOException {
        // Convert to a canonical file (resolves "..", symbolic links, etc.)
        File canonicalFile = file.getCanonicalFile();
        while (canonicalFile != null) {
            if ("target".equals(canonicalFile.getName())) {
                return true;
            }
            canonicalFile = canonicalFile.getParentFile();
        }
        return false;
    }

}
