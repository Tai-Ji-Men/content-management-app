package com.tjm.configmanager;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class TJMConfigManagerFactory {
    private static Logger LOGGER = LoggerFactory.getLogger(TJMConfigManagerFactory.class);

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
            config = new ConfigManager(staticConfiguration);
        } catch (Exception exc) {
            LOGGER.error("err happened. {}", exc);
            throw new RuntimeException("exception happened in TJMConfigManagerFactory");
        }
    }


    private static void loadAllProperties(
            CombinedConfiguration combinedConfiguration)
            throws Exception {
        // calculate the path
//        String tjmCoreAppParentPath = System.getProperty("user.dir");
//        iterateDirectory(combinedConfiguration, tjmCoreAppParentPath);

        // look in src/main/resources/config/*.properties inside your JAR
        Logger LOGGER = LoggerFactory.getLogger(TJMConfigManagerFactory.class);
        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();

        // Only pick up *.properties at the root of your JAR's classpath
        Resource[] rootProps = resolver.getResources("classpath*:/*.properties");
        Resource[] configProps = resolver.getResources("classpath*:configproperties/*.properties");
        Resource[] allProps = Stream.concat(Arrays.stream(rootProps), Arrays.stream(configProps))
                .toArray(Resource[]::new);
        Set<String> usedNames = new HashSet<>();

        for (Resource res : allProps) {
            if (!res.isReadable()) continue;

            String filename = res.getFilename();
            if (filename == null) continue;

            // Skip any POM or META-INF metadata files
            String uri = res.getURI().toString();
            if (uri.contains("/META-INF/")) {
                LOGGER.debug("Skipping non-app config: {}", uri);
                continue;
            }

            // Derive a base name and ensure uniqueness
            String base = FilenameUtils.removeExtension(filename);
            String name = base;
            int suffix = 1;
            while (usedNames.contains(name)) {
                name = base + "_" + (suffix++);
            }
            usedNames.add(name);

            // Load into Commons Configuration
            PropertiesConfiguration props = new PropertiesConfiguration();
            props.setDelimiterParsingDisabled(true);
            try (InputStream in = res.getInputStream()) {
                props.load(in);
            }

            combinedConfiguration.addConfiguration(props, name);
            LOGGER.info("Loaded [{}] from {}", name, uri);
        }

        if (combinedConfiguration.isEmpty()) {
            throw new IllegalStateException(
                    "No .properties found on the classpath – your CombinedConfiguration is empty");
        }
    }

    private static synchronized void iterateDirectory(
            CombinedConfiguration combinedConfiguration, String propertiesPath) {

        try {
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
        }catch(Exception e) {
            LOGGER.error("Error happened in iterateDirectory(). e: {}", e);
            throw new RuntimeException("exception happened in iterateDirectory()");
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
