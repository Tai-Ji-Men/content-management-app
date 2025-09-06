package com.tjm.tjmwebapp.services;

import com.tjm.common.mongo.MongoDBFactory;
import com.tjm.mongo.models.TJMFindIterable;
import com.tjm.mongo.models.TJMMongoCollection;
import org.apache.commons.collections.MapUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@Service
public class TJMNewsService {
    private static final Logger log = LoggerFactory.getLogger(TJMNewsService.class);

    // Adjust to your naming
    private static final String TJM_DB_NAME = "TJM_USA_UIClient";
    private static final String TJM_NEWS_COLLECTION_NAME = "TJMNewsCollection";
    private static final String HOME_CONFIGS = "HomeConfigs";

    /** GET /api/news/home — build payload from HomeConfig + Articles */
    /** GET /api/news/home — build payload from pageConfig + articles */
    public Map<String, Object> getNewsPagePayload() {
        // 1) Load config
        Map<String, Object> cfg = getLatestNewPageConfig();
        if (cfg == null) {
            throw new NoSuchElementException("No pageConfig found in " + TJM_NEWS_COLLECTION_NAME);
        }

        // 2) Extract references
        Map<String, Object> todays = safeMap(cfg.get("todaysPicks"));
        String primaryId = strOrNull(todays.get("primary"));
        List<String> secondaryIds = listOfStrings(todays.get("secondary"));
        List<String> mostRecentIds = listOfStrings(cfg.get("mostRecent"));
        List<Map<String, Object>> topicsCfg = listOfMaps(cfg.get("topics"));

        // 3) Collect all needed article IDs
        Set<String> ids = new LinkedHashSet<>();
        if (primaryId != null) ids.add(primaryId);
        ids.addAll(secondaryIds);
        ids.addAll(mostRecentIds);
        for (Map<String, Object> t : topicsCfg) {
            ids.addAll(listOfStrings(t.get("items")));
        }

        // 4) Fetch article docs into a map
        Map<String, Map<String, Object>> byId = fetchArticlesByIds(ids);

        // 5) Map helpers
        // Turn a full article doc → a small card object the UI needs
        java.util.function.Function<Map<String, Object>, Map<String, Object>> toRef = a -> {
            if (a == null) return null;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", strOrNull(a.get("_id")));
            m.put("slug", strOrNull(a.get("slug")));
            m.put("title", strOrNull(a.get("title")));
            m.put("image", strOrNull(a.get("heroImage")));
            m.put("excerpt", strOrNull(a.get("subtitle")));
            return m;
        };

        // 6) Build Today’s Picks
        Map<String, Object> todaysOut = new LinkedHashMap<>();
        todaysOut.put("primary", toRef.apply(byId.get(primaryId)));
        List<Map<String, Object>> secondaryOut = secondaryIds.stream()
                .map(byId::get).filter(Objects::nonNull).map(toRef).toList();
        todaysOut.put("secondary", secondaryOut);

        // 7) Build Most Recent (fallback to latest 3 published if not curated)
        List<Map<String, Object>> mostRecentOut = new ArrayList<>();
        if (!mostRecentIds.isEmpty()) {
            mostRecentOut = mostRecentIds.stream()
                    .map(byId::get).filter(Objects::nonNull).map(toRef).toList();
        } else {
            // Fallback: scan all published articles in the collection, pick top 3 by publishedAt

            // TODO: impl logic if most recent section is not in the config doc.

//            mostRecentOut = fetchTopPublishedArticles(3).stream().map(toRef).toList();
        }

        // 8) Build Topics array
        List<Map<String, Object>> topicsOut = new ArrayList<>();
        for (Map<String, Object> t : topicsCfg) {
            String title = strOrNull(t.get("title"));
            String image = strOrNull(t.get("image"));
            List<String> itemIds = listOfStrings(t.get("items"));
            List<Map<String, Object>> items = itemIds.stream()
                    .map(byId::get).filter(Objects::nonNull).map(toRef).toList();

            Map<String, Object> tOut = new LinkedHashMap<>();
            tOut.put("id", title != null ? title : UUID.randomUUID().toString());
            tOut.put("title", title);
            tOut.put("image", image);
            tOut.put("items", items);
            topicsOut.add(tOut);
        }

        // 9) Assemble payload
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("todaysPicks", todaysOut);
        payload.put("mostRecent", mostRecentOut);
        payload.put("topics", topicsOut);

        return payload;
    }

    /** GET /api/news/articles/{slug} — details or 301 if slug moved */
    public ResponseEntity<?> getArticleBySlug(String slug) {
        Map<String, Object> article = findArticleBySlug(slug);
        if (article != null) {
            return ResponseEntity.ok(mapArticleDetails(article));
        }
        // try slugHistory contains
        Map<String, Object> moved = findArticleByOldSlug(slug);
        if (moved != null) {
            String newSlug = strOrNull(moved.get("slug"));
            return ResponseEntity.status(301).location(URI.create("/news/" + newSlug)).build();
        }
        return ResponseEntity.status(404).body(Map.of("error",
                Map.of("code", "NOT_FOUND", "message", "Article not found")));
    }

    // ---------------------- Mongo helpers (raw) ----------------------

    private Map<String, Object> getLatestNewPageConfig() {
        TJMMongoCollection newsCollection = MongoDBFactory.getAcctCollection(TJM_DB_NAME, TJM_NEWS_COLLECTION_NAME);
        // find the new page payload config. use mock here.
        Map<String, Object> fetchQuery = new HashMap<>();
        fetchQuery.put("type", "pageConfig");
        Map<String, Object> configMap = newsCollection.findOne(fetchQuery);
        if(!MapUtils.isEmpty(configMap)) {
            return configMap;
        }else {
            return null;
        }
    }

    private Map<String, Map<String, Object>> fetchArticlesByIds(Collection<String> ids) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        if (ids == null || ids.isEmpty()) return result;

        // If your driver supports $in:
        // Map<String,Object> q = Map.of("_id", Map.of("$in", new ArrayList<>(ids)));
        // TJMFindIterable it = MongoDBFactory.getAcctCollection(DB, ARTICLES).find(q);

        // Portable fallback: get per id
        TJMMongoCollection newsCollection = MongoDBFactory.getAcctCollection(TJM_DB_NAME, TJM_NEWS_COLLECTION_NAME);
        for (String id : ids) {
            Map<String, Object> query = new HashMap<>();
            query.put("_id", new ObjectId(id));
            Map<String, Object> articleReturned = newsCollection.findOne(query);
            if(MapUtils.isNotEmpty(articleReturned)) {
                result.put(id, articleReturned);
            }
        }
        return result;
    }

    private Map<String, Object> findArticleBySlug(String slug) {
        TJMMongoCollection newsCollection = MongoDBFactory.getAcctCollection(TJM_DB_NAME, TJM_NEWS_COLLECTION_NAME);
        Map<String, Object> fetchQuery = new HashMap<>();
        fetchQuery.put("slug", slug);
        Map<String, Object> returnedArticle = newsCollection.findOne(fetchQuery);
        return returnedArticle;
    }

    private Map<String, Object> findArticleByOldSlug(String oldSlug) {
        TJMMongoCollection col = MongoDBFactory.getAcctCollection(TJM_DB_NAME, TJM_NEWS_COLLECTION_NAME);
        // query array contains
        Map<String, Object> q = new HashMap<>();
        q.put("slugHistory", oldSlug);
        TJMFindIterable it = col.find(q);
        var iterator = it.iterator();
        return iterator.hasNext() ? castMap(iterator.next()) : null;
    }

    // ---------------------- mapping helpers ----------------------

    /** Article → ArticleRef for home cards */
    private Map<String, Object> mapArticleRef(Map<String, Object> a) {
        if (a == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", strOrNull(a.get("_id")));
        m.put("slug", strOrNull(a.get("slug")));
        m.put("title", strOrNull(a.get("title")));
        m.put("image", strOrNull(a.get("heroImage"))); // using heroImage for card
        m.put("excerpt", strOrNull(a.get("subtitle"))); // short copy
        return m;
    }

    /** Full details for /articles/{slug} */
    private Map<String, Object> mapArticleDetails(Map<String, Object> a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", strOrNull(a.get("_id")));
        m.put("slug", strOrNull(a.get("slug")));
        m.put("title", strOrNull(a.get("title")));
        m.put("subtitle", strOrNull(a.get("subtitle")));
        m.put("heroImage", strOrNull(a.get("heroImage")));
        m.put("publishedAt", a.get("publishedAt"));
        m.put("category", strOrNull(a.get("category")));
        m.put("tags", listOfStrings(a.get("tags")));

        // Body blocks are stored as an array of objects
        List<Map<String, Object>> bodyBlocks = listOfMaps(a.get("body"));
        // (Optionally validate/sanitize if you allow 'html' blocks)
        m.put("body", bodyBlocks);

        return m;
    }

    // ---------------------- small utils ----------------------

    private Map<String, Object> castMap(Object o) {
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) o;
        return m == null ? new HashMap<>() : m;
    }

    private Map<String, Object> safeMap(Object o) {
        if (o instanceof Map) return castMap(o);
        return new HashMap<>();
    }

    private List<Map<String, Object>> listOfMaps(Object o) {
        if (!(o instanceof List<?> l)) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object e : l) if (e instanceof Map) out.add(castMap(e));
        return out;
    }

    private List<String> strList(Object o) {
        if (!(o instanceof List<?> l)) return List.of();
        List<String> out = new ArrayList<>();
        for (Object e : l) if (e != null) out.add(String.valueOf(e));
        return out;
    }

    private List<String> listOfStrings(Object o) {
        if (!(o instanceof List<?> l)) return List.of();
        return l.stream().filter(Objects::nonNull).map(String::valueOf).toList();
    }

    private String strOrNull(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Instant instOrEpoch(Object o) {
        try {
            if (o instanceof Date d) return d.toInstant();
            if (o instanceof Long l) return Instant.ofEpochMilli(l);
            if (o instanceof String s) return Instant.parse(s);
        } catch (Exception ignore) {}
        return Instant.EPOCH;
    }
}
