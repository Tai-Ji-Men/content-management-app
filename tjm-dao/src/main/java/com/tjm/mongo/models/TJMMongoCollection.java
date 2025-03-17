package com.tjm.mongo.models;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.tjm.mongo.common.Util;
import com.tjm.mongo.common.UtilAttributePattern;
import com.tjm.mongo.utils.Utils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TJMMongoCollection {

    private static final String OUTPUT_TYPE = "Output Type";

    private static final String OUTPUT_TARGET = "Output Target";

    private static final String OUTPUT_DATABASE = "Output Database";

    private static final String DISTINCT_KEY = "Distinct Key";

    private static final String REDUCE_FUNCTION = "Reduce Function";

    private static final String MAP_FUNCTION = "Map Function";

    private static final String OPTIONS = "Options";

    private static final String UNCHECKED="unchecked";

    private static final String RAW_TYPES="rawtypes";

    private static final String INSERT_COUNT_STR = "Insert Count";

    private static final String COLLECTION_NAME_STR = "Collection Name";

    private static final String PROJECTION_STR = "Projection";

    private static final String INDEX_KEYS_STR = "Index Keys";

    private static final String COLLECTION_CURRENT_NAME_STR = "Collection Current Name";

    private static final String COLLECTION_NEW_NAME_STR = "Collection New Name";

    private static final String MATCHED_COUNT_STR = "Matched Count";

    private static final String MODIFIED_COUNT_STR = "Modified Count";

//    private static final ILogService LOGGER = LogFactoryImpl.getInstance().getLoggerService(ReconMongoCollection.class);

    private MongoCollection<Document> collection;
    private static final String INDEX_NAME = "name";
    private static final String LOG_CRUD = "MONGO DB OPERATION : {}, START TIME : {}, END TIME : {}, TIME TAKEN : {}ms , FILTER : {}, UPDATE PARAMS: {}, OUTPUT : {}, WRITECONCERN: {}, OPTIONS:{}, MULTI DOCUMENT OPERATION: {}, COLLECTIONNAME: {}";
    private static final String LOG_AGGREGATION = "MONGO DB OPERATION : {}, START TIME : {}, END TIME : {}, TIME TAKEN : {}ms , PIPELINE: {}, HINTS : {}";
    private static final String LOG_GENERAL = "MONGO DB OPERATION : {}, START TIME : {}, END TIME : {}, TIME TAKEN : {}ms , FILTER : {}, PARAMS {}, OUTPUT: {}, WRITECONCERN: {}, COLLECTIONNAME: {} ";

    protected TJMMongoCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    protected MongoCollection<Document> toDBCollection() {
        return collection;
    }


    public ObjectId insert(Map<String, Object> o, TJMWriteConcern concern) {
        final Instant startInstant = Instant.now();
        ObjectId objId = Utils.getId(o);
        try {
            // todo: isSingleAckOk??? --> research
            collection.withWriteConcern(concern.toWriteConcern(false)).insertOne(UtilAttributePattern.massageMap(o));
            return objId;
        } catch (MongoException mongoException) {
            throw new RuntimeException(mongoException);
        } finally {
            Map<String, Object> output = new HashMap<>();
            output.put("Inserted ObjectId", objId);
//            logCrudOperations(OPERATIONTYPE.INSERT, startInstant, null, null, output, null, concern, false,collection.getNamespace().getCollectionName());
        }
    }

    public Map<String, Object> findOne(Map<String, Object> query) {
        return this.commonFindOne(query, null);
    }

    private Map<String, Object> commonFindOne(Map<String, Object> map, Map<String, Object> projection) {
        final Instant startInstant = Instant.now();
        try {
            FindIterable<Document> f = projection != null
                    ? collection.find(Util.massageMap(map)).projection(Util.massageMap(Util.fixInvalidProjectFields(projection)))
                    : collection.find(Util.massageMap(map));
            if (f != null) {
                return f.first();
            }
            return Collections.emptyMap();
        } catch (MongoException mongoException) {
            throw new RuntimeException(mongoException);
        } finally {
            Map<String, Object> options = new HashMap<>();
            options.put(PROJECTION_STR, projection);
//            logCrudOperations(OPERATIONTYPE.FIND, startInstant, map, null, null, options, null, false,collection.getNamespace().getCollectionName());
        }
    }








}
