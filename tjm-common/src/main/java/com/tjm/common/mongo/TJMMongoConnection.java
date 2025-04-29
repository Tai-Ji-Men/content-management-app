package com.tjm.common.mongo;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.tjm.common.exceptions.MongoConnectionException;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

public class TJMMongoConnection {
    public static void connect() throws MongoConnectionException {
        String connectionString = "mongodb+srv://edwardtao1994:taojiayou1994@cluster0.uthl9.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
        // Create a new client and connect to the server

        MongoClient mongoClient = MongoClients.create(settings);
        // Cache it for reuse
        MongoDBFactory.cacheConnections(mongoClient);
        try {
            // Send a ping to confirm a successful connection
            MongoDatabase database = mongoClient.getDatabase("test");
            database.runCommand(new Document("ping", 1));

            MongoCollection<Document> testCollection = database.getCollection("test123");
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("name", "John Doe");
            dataMap.put("age", 30);
            dataMap.put("city", "New York");

            // Convert the Map to a Document
            Document document = new Document(dataMap);
            testCollection.insertOne(document);

            // set the Mongo client to cache it.
            MongoDBFactory.cacheConnections(mongoClient);
            System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
        } catch (Exception e) {
            System.out.println("Error when connecting to Mongo DB!!!");
            e.printStackTrace();
        }
    }
}
