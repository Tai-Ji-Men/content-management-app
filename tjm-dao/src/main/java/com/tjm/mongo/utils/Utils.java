package com.tjm.mongo.utils;

import com.mongodb.BasicDBList;
import com.mongodb.Tag;
import com.mongodb.TagSet;
import com.tjm.configmanager.TJMConfigManagerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.BsonUndefined;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    private static Logger logger = LoggerFactory.getLogger(Utils.class);

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

    public static Document massageMap(final Map<String, Object> map) {

        if(map != null && !map.isEmpty())
        {
            if(map instanceof Document )
            {
                return (Document) map;
            }
            if( map.entrySet() instanceof Map.Entry<?, ?>)
            {
                for(Map.Entry<String, Object> entry : map.entrySet()) {
                    Object v = entry.getValue();
                    String k = entry.getKey();
                    if (v instanceof Map) {
                        map.put(k, new Document((Map)v));
                        massageMap((Map) v);
                    } else if (v instanceof List) {
                        massageList((List) v);
                    }
                }
            }
            return new Document(map);
        }else
        {
            return new Document(new HashMap<String, Object>());
        }
    }

    public static BasicDBList massageList(List<Object> li) {
        BasicDBList basicDBList = new BasicDBList();
        for(Object o : li) {
            if (o instanceof Map) {
                basicDBList.add(massageMap((Map)o));
            } else if (o instanceof List) {
                basicDBList.add(massageList((List) o));
            } else {
                basicDBList.add(o);
            }
        }
        return basicDBList;
    }

    public static List<Document> massageMaps(final List<Map<String, Object>> maps) {

        List<Document> dbObjects = new ArrayList<>(maps.size());
        try{
            for(Map<String, Object> map : maps) {
                dbObjects.add(massageMap(map));
            }
        }catch(Exception e){
            logger.error("Error occured in  massageMaps method, exception = ", e);
        }
        return dbObjects;
    }

    public static List<Map<String, Object>> massageIterableDBObjectToListOfMap(final Iterable<Document> dbObjects) {
        List<Map<String, Object>> dbObjectsList = new ArrayList<Map<String, Object>>();
        for(Document dbObject : dbObjects)
        {
            dbObjectsList.add(dbObject);
        }
        return dbObjectsList;
    }


//     @SuppressWarnings("unchecked")
// 	public static Map<String, Object> contentsToString(final Map<String, Object> map) {
//         Map<String, Object> result = new HashMap<>(map.size());
//         for(Map.Entry<String, Object> entry : result.entrySet()) {
//             String k = entry.getKey();
//             Object v = (entry.getValue() == null || (entry.getValue() instanceof String && entry.getValue().toString().isEmpty())) ? "\"\"" : entry.getValue();
//
//             if (v instanceof Map) {
//                 contentsToString((Map<String, Object>) v);
//             }
//             result.put(k, v instanceof String ? v : JSON.serialize(v));
//         }
//         logger.info("BFS: b4 {} and after {}", map, result);
//         return result;
//     }


    public static HashMap<String, Object> convertDocumentToHashMap(final Map<String, Object> map)
    {
        HashMap<String, Object> hashMap = new HashMap<>(map.size());
        if(map instanceof Document || map instanceof Map)
        {
            Map<String, Object> doc = (Map<String, Object>)map;
            if(doc != null && !doc.isEmpty())
            {
                hashMap.putAll(doc);
            }

        }else if(map instanceof HashMap)
        {
            return (HashMap<String, Object>)map;
        }

        return hashMap;
    }


    public static String toJson(final Map<String, Object> map)
    {
        return massageMap(map).toJson();
    }

    /*
     * Added for Mongo 4.4 upgrade. Intended as temporary solution until the corresponding code is permanently fixed.
     * In 4.2 and earlier versions, Non numeric values and non zero numbers are treated as 1, Zero value numbers and null value projection is treated as 0
     * In 4.4 and later versions, Non numeric values and null are projected as is, non zero number is treated as 1, Zero value numbers are treated as 0
     * Method will update projection fields with non numeric values to revert to the 4.2 behavior.
     * Method will also identify potential collisions in project fields
     *
     */
    public static Map<String,Object> fixInvalidProjectFields(Map<String,Object> projection)
    {
        if(MapUtils.isNotEmpty(projection))
        {
            /*
             * TODO: Uncomment lines below and return projectFields for sep release
             * https://quip-apple.com/ahgdAk5zXOqi
             */
            boolean hasInvalidValues = false;
            boolean hasInvalidKeys = false;
            try {
                //Map<String,Object> projectFields=new HashMap<>();
                for (Map.Entry<String, Object> entry : projection.entrySet()) {
                    /*
                     * Check for blank keys and keys with $ value specified in projection
                     */
                    if (StringUtils.isBlank(entry.getKey()) || StringUtils.contains(entry.getKey(), "$")) {
                        hasInvalidKeys = true;
                    }

                    /*
                     * Check for non numeric values. 4.4 changes the behavior for non-numeric values
                     */
                    // Object value;
                    if (entry.getValue() == null) {
                        hasInvalidValues = true;
                        // value=0;
                    } else if (!(entry.getValue() instanceof Number)) {
                        hasInvalidValues = true;
                        // value=1;
                    }
                    //else
                    //{
                    //	value=List.of(0,0.0,0f).contains(entry.getValue())?0:1;
                    //}
                    // projectFields.put(entry.getKey(), value);
                }

                if (hasInvalidValues) {
                    logger.warn("Illegal project fields: Projection for query contain invalid values {}",projection.values());
                }
                if (hasInvalidKeys) {
                    logger.warn("Illegal project fields: Projection for query contain invalid keys  {}",projection.keySet());
                }

                //Check for outer key and nested key collisions here
                if (CollectionUtils.containsAny(
                        projection.keySet().stream().filter(f -> !f.contains(".")).collect(Collectors.toSet()),
                        projection.keySet().stream().filter(f -> f.contains(".")).map(f -> f.split("\\.")[0]).collect(Collectors.toSet()))) {
                    logger.warn("Illegal project fields: Potential collision of embedded document outer keys with nested keys {}",projection.keySet());
                }
                //return projectFields;
            } catch (Exception e) {
                // Suppress exceptions
                logger.error("Illegal project fields: Exception occured while trying to check projection {}: {}",
                        projection, e.getMessage());
            }
        }
        return projection;
    }


    /*
     * Added for Mongo 4.4 upgrade.Intended as temporary solution until the corresponding code is permanently fixed.
     * In 4.2 and earlier versions, For sort on fields containing duplicate values, Mongo automatically followed natural ordering based on _id
     * In 4.4 sort on _id or another unique field will need to be added explicitly
     *
     */
    public static LinkedHashMap<String, Object> appendSortOnId(LinkedHashMap<String, Object> sort)
    {
        if(MapUtils.isEmpty(sort) || sort.keySet().stream().anyMatch(key->key.startsWith("_id"))
                || !"true".equalsIgnoreCase(TJMConfigManagerFactory.getConfigManager().getPropertyString("enable.mongo.appendSortOnId","true"))) {

            //return passed object as-is if provided sort is empty, contains _id or _id. fields or if the option is explicitly disabled
            return sort;
        }
        LinkedHashMap<String, Object> sortFields=new LinkedHashMap<>(sort);
        sortFields.put("_id", 1);
        logger.warn("_id not present in sort object,appending to sort");
        return sortFields;
    }


}
