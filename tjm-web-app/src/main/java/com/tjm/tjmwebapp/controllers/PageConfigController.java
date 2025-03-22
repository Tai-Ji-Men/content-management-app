package com.tjm.tjmwebapp.controllers;

import com.tjm.common.mongo.MongoDBFactory;
import com.tjm.mongo.models.TJMMongoCollection;
import com.tjm.mongo.models.TJMWriteConcern;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
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
        Map<String, Object> fetchQuery = new HashMap<>();
        if(StringUtils.isEmpty(pageName)) {
            Map<String, Object> errorRes = new HashMap<>();
            errorRes.put("status", "404");
            errorRes.put("message", "type is missing in the request.");
            return ResponseEntity.badRequest().body(errorRes);
        }
        if(StringUtils.isNotEmpty(subPage)) {
            fetchQuery.put("subType", subPage);
        }
        try {
            // Simulate fetching configuration from MongoDB
            TJMMongoCollection uiConfigCollection = MongoDBFactory.getAcctCollection("TJM", "TJMUIConfig");
            Map<String, Object> configRes = uiConfigCollection.findOne(fetchQuery);
            return ResponseEntity.ok(configRes);
        } catch (Exception e) {
            // Handle unexpected exceptions with a 500 Internal Server Error response
            Map<String, Object> errorRes = new HashMap<>();
            errorRes.put("status", "500");
            errorRes.put("message", "Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorRes);
        }
    }
}
