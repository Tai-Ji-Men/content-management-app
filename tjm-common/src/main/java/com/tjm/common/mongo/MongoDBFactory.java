package com.tjm.common.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.tjm.common.constants.MongoConnectionConstants;
import com.tjm.common.exceptions.MongoConnectionException;
import com.tjm.configmanager.ConfigManager;
import com.tjm.configmanager.TJMConfigManagerFactory;
import com.tjm.mongo.models.TJMMongoCollection;
import com.tjm.mongo.models.TJMMongoDatabase;

import java.util.HashMap;
import java.util.Map;

public final class MongoDBFactory {
    private static Map<String, TJMMongoDatabase> cacheMongoDBs = new HashMap<>();
    private static MongoClient cachedMongoClient;

    private static ConfigManager config;
    private static String tempCollectionName = "";
    private static String reducedCollectionName = "";
    private static String exceptionCollectionName = "";

    private static String serverVersion;

    public static synchronized TJMMongoDatabase getMongoDB(String dbName) {
        TJMMongoDatabase db = null;
        // TODO: using the caching mechanism for speeding up the process
        if(cacheMongoDBs.get(dbName) != null) {
            return cacheMongoDBs.get(dbName);
        }else {
            // get the db and cache it into cacheMongoDBs map
            TJMMongoDatabase mongoDB = new TJMMongoDatabase(cachedMongoClient.getDatabase(dbName));
            cacheMongoDBs.put(dbName, mongoDB);
            return mongoDB;
        }
    }

    public static synchronized TJMMongoCollection getAcctCollection(String dbName, String collectionName)  {
        if(config == null){
            config = TJMConfigManagerFactory.getConfigManager();
        }
        return getMongoDB(dbName).getCollection(collectionName);
    }

    /*
     * this method creates and caches the mongodb connections
     */
    public static void cacheConnections(MongoClient mongoClient) throws MongoConnectionException {
        final String authType = TJMConfigManagerFactory.getConfigManager().getPropertyString("mongo.credentials.auth.type", MongoConnectionConstants.MongoAuthenticationTypes.X509.toString());
//        MongoConnectionBuilder mongoConnectionBuilder = null ;
//        if(authType.equalsIgnoreCase(MongoConnectionConstants.MongoAuthenticationTypes.X509.toString())){
//            mongoConnectionBuilder = new MongoX509ConnectionBuilder();
//        }
//        else {
//            throw new MongoConnectionException ("Invalid authtype: "+authType);
//        }
//        if(null != mongoConnectionBuilder){
//            cacheAccountLevelMongoDBs = mongoConnectionBuilder.build();
//        }
        // return cached db
        cachedMongoClient = mongoClient;
    }

}
