package com.tjm.tjmwebapp.controllers;

import com.tjm.common.mongo.MongoDBFactory;
import com.tjm.mongo.models.TJMMongoCollection;
import com.tjm.mongo.models.TJMMongoDatabase;
import com.tjm.mongo.models.TJMWriteConcern;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
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

}
