package com.tjm.mongo.models;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.bson.BsonDocument;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TJMMongoDatabase {
    private MongoDatabase mongodb;
    private com.mongodb.client.MongoClient newMongoClient;
    private Set<String> mongoHostsPorts;


    protected TJMMongoDatabase(MongoDatabase mongodb, com.mongodb.client.MongoClient newMongoClient, Set<String> mongoHostsPorts) {
        this.mongodb = mongodb;
        this.newMongoClient = newMongoClient;
        this.mongoHostsPorts = mongoHostsPorts;
    }

    public TJMMongoDatabase (MongoDatabase mongodb) {
        this.mongodb = mongodb;
    }

    public Set<String> getMongoHosts() {
        return mongoHostsPorts;
    }

    // Get mongoClient to start the session
    public com.mongodb.client.MongoClient getMongoClientNew() {
        return this.newMongoClient;
    }

    public Map<String,Object> runCommand(BsonDocument command) {
        return this.mongodb.runCommand(command);
    }

    public TJMMongoCollection getCollection(String collection) {
        return new TJMMongoCollection(mongodb.getCollection(collection));
    }

    public String getName() {
        return mongodb.getName();
    }

    public void setWriteConcern(TJMWriteConcern writeConcern) {
        mongodb.withWriteConcern(writeConcern.toWriteConcern(false));
    }

    public TJMWriteConcern getWriteConcern() {
        return TJMWriteConcern.ACKNOWLEDGED;
    }

    public void setReadPreference(TJMReadPreference readPreference) {
        mongodb.withReadPreference(readPreference.toReadPreference());
    }

    public TJMReadPreference getReadPreference() {
        return TJMReadPreference.primaryPreferred;
    }

    public Iterator<String> listCollectionNames() {
        return mongodb.listCollectionNames().iterator();
    }

    public void dropDatabase() {
        mongodb.drop();
    }

    public void createCollection(String collectionName) {
        mongodb.createCollection(collectionName);
    }

    public GridFSBucket getGridFsBucket(String bucketName) {
        GridFSBucket bucket = null;
        if (bucketName != null && !bucketName.isEmpty()) {
            bucket = GridFSBuckets.create(this.mongodb, bucketName);
        } else {
            bucket = GridFSBuckets.create(this.mongodb);
        }
        return bucket;
    }

    @Override
    public String toString() {
        return "DB Name=" + getName();
    }


}
