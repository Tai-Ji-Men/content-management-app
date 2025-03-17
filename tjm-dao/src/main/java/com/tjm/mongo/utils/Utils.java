package com.tjm.mongo.utils;

import com.mongodb.Tag;
import com.mongodb.TagSet;
import org.bson.BsonUndefined;
import org.bson.types.ObjectId;

import java.util.*;

public class Utils {

    public static final String ESCAPED_PERIOD = "\\.";

    public static ObjectId getId(Map<String, Object> o) {
        ObjectId _id;
        if (o.get(Constants._ID) == null) {
            _id = new ObjectId();
            o.put(Constants._ID, _id);
        } else {
            if (o.get(Constants._ID) instanceof ObjectId) {
                _id = (ObjectId) o.get(Constants._ID);
            } else {
                _id = new ObjectId(o.get(Constants._ID).toString());
            }
        }
        return _id;
    }

    public static Object getId(Map<String, Object> o, boolean overrideObjectIdValidation) {
        ObjectId _id;
        if (o.get(Constants._ID) == null) {
            _id = new ObjectId();
            o.put(Constants._ID, _id);
        } else {
            if (o.get(Constants._ID) instanceof ObjectId) {
                _id = (ObjectId) o.get(Constants._ID);
            } else {
                try {
                    _id = new ObjectId(o.get(Constants._ID).toString());
                } catch (IllegalArgumentException e) {
                    if (overrideObjectIdValidation) {
                        return o.get(Constants._ID);
                    }
                    throw e;
                }
            }
        }
        return _id;
    }

    public static Object extractValue(Map<String, Object> inData, String param) {
        Object value = inData;
        String[] params = param.split(ESCAPED_PERIOD);
        for(String p : params) {
            if (((Map<String, Object>) value).containsKey(p)) {
                value = ((Map<String, Object>) value).get(p);
            } else {
                value = null;
                break;
            }
            if (value == null) {
                break;
            }
        }
        return value;
    }

    public static boolean isTypeBsonUndefined(Object o) {
        if (o == null || o instanceof BsonUndefined) {
            return true;
        }

        return false;
    }

    public static List<TagSet> buildTagSet(List<Map<String, String>> tagValues) {

        List<TagSet> tagSets = new ArrayList<>();
        List<Tag> tags = new ArrayList<>();
        // handle the exception
        if (tagValues == null || tagValues.size() == 0) {

        } else {
            for (Map<String, String> tag : tagValues) {
                Set<String> keys = tag.keySet();
                for (String key : keys) {
                    tags.add(new Tag(key, tag.get(key)));
                    tagSets.add(new TagSet(tags));
                }
            }
        }
        return tagSets;

    }

    public static void iterateAndProcess(FuncType func, Iterator<?> cursor, int batchSize, Map<String, Object> params) throws Exception {

        int totalCounter = 0;
        List<Map<String, Object>> batch = new ArrayList<>();
        while(cursor.hasNext()) {
            Map<String, Object> doc = (Map<String, Object>) cursor.next();
            totalCounter++;
            batch.add(doc);

            if (batch.size() == batchSize) {
                params.put(Constants.BATCH, batch);
                params.put(Constants.COUNT, totalCounter);
                func.call(params);
                batch = new ArrayList<>();
            }
        }

        if (!batch.isEmpty()) {
            params.put(Constants.BATCH, batch);
            params.put(Constants.COUNT, totalCounter);
            func.call(params);
        }
    }


}
