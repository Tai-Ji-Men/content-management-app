package com.tjm.tjmwebapp.controllers;

import ch.qos.logback.core.util.StringUtil;
import com.tjm.common.mongo.MongoDBFactory;
import com.tjm.mongo.models.TJMMongoCollection;
import com.tjm.mongo.models.TJMMongoDatabase;
import com.tjm.mongo.models.TJMWriteConcern;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tjm/api/v1")
public class PageConfigController {

    // create a GET API handler.
    public PageConfigController() {
        // nothing
    }

    @GetMapping("/test")
    public String testHandler() {
        System.out.println("testHandler got triggered!!");
        TJMMongoCollection testCollection = MongoDBFactory.getAcctCollection("test", "test");
        Map<String, Object> map = new HashMap<>();
        map.put("test123", "aaaabbbbcccc");
        testCollection.insert(map, TJMWriteConcern.ACKNOWLEDGED);
        return "test";
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig(
            @RequestParam(name = "type") String pageName,
            @RequestParam(name = "subType", required = false) String subPage) {
        TJMMongoCollection uiConfigCollection = MongoDBFactory.getAcctCollection("TJM", "TJMUIConfig");
        Map<String, Object> fetchQuery = new HashMap<>();
        if(StringUtils.isNotEmpty(pageName)) {
            fetchQuery.put("type", pageName);
        }
        if(StringUtils.isNotEmpty(subPage)) {
            fetchQuery.put("subType", subPage);
        }
        Map<String, Object> configRes = uiConfigCollection.findOne(fetchQuery);
        return ResponseEntity.ok(configRes);
    }
}
