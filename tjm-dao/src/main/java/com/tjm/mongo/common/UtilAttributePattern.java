package com.tjm.mongo.common;

import com.mongodb.BasicDBList;
import com.tjm.mongo.utils.Constants;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import com.tjm.mongo.utils.Utils;
import java.util.*;

public class UtilAttributePattern {
//    private static final ILogService logger = LogFactoryImpl.getInstance().getLoggerService(UtilAttributePattern.class);

    private static Set<Map.Entry<String, String>> attributePatternFieldsAliases = Collections.EMPTY_MAP.entrySet();

    public static void setAttributePatternFields(String prop) {
//        logger.info("setAttributePatternFields called with prop {}", prop);
        if (StringUtils.isEmpty(prop)) {
            return;
        }
        Map<String, String> tempAttributePatternFieldsAliases = new HashMap<>();
        String[] fields = prop.split(Constants.COMMA);
        for(String field : fields) {
            String name = field;
            String alias = field;
            if (field.contains(Constants.COLON)) {
                String[] s = field.split(Constants.COLON);
                name = s[0];
                alias = s[1];
            }
            tempAttributePatternFieldsAliases.put(name, alias);
        }
        attributePatternFieldsAliases = tempAttributePatternFieldsAliases.entrySet();
//        logger.info("setAttributePatternFields fields and aliases {}", attributePatternFieldsAliases);
    }

    private static Document massageMapWithAttributesFields(final Document doc) {
        if (doc.isEmpty() || attributePatternFieldsAliases.isEmpty()) {
            return doc;
        }
        for(Map.Entry<String, String> e : attributePatternFieldsAliases) {
            copyToAttributes(doc, e.getKey(), e.getValue());
        }
        return doc;
    }


    private static void copyToAttributes(Map<String, Object> doc, String field, String alias) {
        if (!doc.containsKey(field)) {
            return;
        }
        Object val = Utils.extractValue(doc, field);
        if (val == null) {
            return;
        }
        List<Map<String, Object>> attrs = (List<Map<String, Object>>) doc.get("attrs_arr");
        if (attrs == null) {
            attrs = new ArrayList<>();
        }
        Map<String, Object> attr = new LinkedHashMap<>();
        attr.put("k", alias);
        attr.put("v", val);
        attrs.add(attr);
        doc.put("attrs_arr", attrs);
    }

    public static Document massageMap(final Map<String, Object> map) {

        if(map != null && !map.isEmpty())
        {
            if(map instanceof Document )
            {
                return massageMapWithAttributesFields((Document) map);
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
            return massageMapWithAttributesFields(new Document(map));
        }else
        {
            return massageMapWithAttributesFields(new Document(new HashMap<>()));
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
//            logger.error("Error occured in  massageMaps method, exception = ", e);
        }
        return dbObjects;
    }
}
