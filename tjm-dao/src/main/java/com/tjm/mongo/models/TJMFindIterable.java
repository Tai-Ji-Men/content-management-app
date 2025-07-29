package com.tjm.mongo.models;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.tjm.mongo.common.Util;
import com.tjm.mongo.utils.Utils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class TJMFindIterable implements Iterable{
    /** Instance of Logger */
    private static Logger logger = LoggerFactory.getLogger(TJMFindIterable.class);

    private FindIterable<Document> findIterable;

    private MongoCursor<Document> mongoCursor;

    protected TJMFindIterable(FindIterable<Document> findIterable) {
        this.findIterable = findIterable;
    }

    public TJMFindIterable batchSize(int batchSize) {
        this.findIterable = findIterable.batchSize(batchSize);
        return this;
    }

    public TJMFindIterable skip(int skip) {
        this.findIterable = findIterable.skip(skip);
        return this;
    }

    @SuppressWarnings("unchecked")
    public TJMFindIterable modifiers(Map<String, Object> modifiers) {

        //check for empty or null map
        if(modifiers == null || modifiers.isEmpty()) {
//            logger.warn("Modifiers map is either empty or null "+ modifiers);
            return this;
        }

        //throw an exception if modifiers such as $hint and/or $explain missing
        if (!modifiers.containsKey("$hint") && !modifiers.containsKey("$explain")) {
            throw new RuntimeException("Only $hint and/or $explain is supported in modifiers!");
        }

        if (modifiers.containsKey("$hint")) {
            Object hintObject = modifiers.get("$hint");

            if (hintObject instanceof Map) {
                this.findIterable = findIterable.hint(Util.massageMap((Map<String, Object>) hintObject));

            } else if (hintObject instanceof String) {
                this.findIterable = findIterable.hintString((String) hintObject);

            } else {
                throw new RuntimeException("$hint should have only String or Map!");
            }
        }

        //check $explain if it exist then add explain to the findIterable
        if(modifiers.containsKey("$explain")) {
            explain();
        }

        return this;
    }

    private TJMFindIterable hint(Map<String, Object> hintObject) {
        this.findIterable = findIterable.hint(Util.massageMap(hintObject));
        return this;
    }

    private TJMFindIterable hint(String hintObject) {
        this.findIterable = findIterable.hintString(hintObject);
        return this;
    }

    public TJMFindIterable limit(int limit) {
        this.findIterable = findIterable.limit(limit);
        return this;
    }

    public TJMFindIterable sort(LinkedHashMap<String, Object> sort) {

        this.findIterable = findIterable.sort(Util.massageMap(Util.appendSortOnId(sort)));
        return this;
    }

    public Iterator<?> iterator() {
        mongoCursor = findIterable.iterator();
        return mongoCursor;
    }

    public void close() {
        try {
            if (mongoCursor != null) {
                mongoCursor.close();
            }
        } catch (Exception e) {
//            logger.error("Error while closing mongo cursor", e);
        }
    }

    public TJMFindIterable filter(Map<String, Object> filter) {
        this.findIterable = findIterable.filter(Util.massageMap(filter));
        return this;
    }

    public TJMFindIterable noCursorTimeout(boolean noCursorTimeout) {
        this.findIterable = findIterable.noCursorTimeout(noCursorTimeout);
        return this;
    }

    public Map<String,Object> first() {
        return findIterable.first();
    }

    public Document explain() {
        return findIterable.explain();
    }

    @Override
    public String toString() {
        return "ReconFindIterable [findIterable=" + findIterable + "]";
    }
}
