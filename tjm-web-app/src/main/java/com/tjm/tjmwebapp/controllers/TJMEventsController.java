package com.tjm.tjmwebapp.controllers;

import com.tjm.common.mongo.MongoDBFactory;
import com.tjm.configmanager.TJMConfigManagerFactory;
import com.tjm.mongo.models.TJMFindIterable;
import com.tjm.mongo.models.TJMMongoCollection;
import com.tjm.mongo.models.TJMWriteConcern;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/tjm/api/v1")
public class TJMEventsController {

    private static Logger LOGGER = LoggerFactory.getLogger(PageConfigController.class);

    @GetMapping("/events")
    public ResponseEntity<List<Map<String, Object>>> getEvents() {
        LOGGER.info("getEvents() got triggered!!");
        List<Map<String, Object>> resultList = new ArrayList<>();
        try {
            TJMMongoCollection tjmEventsCollection = MongoDBFactory.getAcctCollection("TJM_USA_UIClient", "TJMEvents");
            Map<String, Object> fetchQuery = new HashMap<>();
            TJMFindIterable it = tjmEventsCollection.find(fetchQuery);
            Iterator itr = it.iterator();
            while (itr.hasNext()) {
                Map<String, Object> recordData = (Map<String, Object>) itr.next();
                resultList.add(recordData);
            }
            return ResponseEntity.ok(resultList);
        }catch(Exception e) {
            String errorMsg = e.getMessage();
            Map<String, Object> errorRes = new HashMap<>();
            errorRes.put("status", "500");
            errorRes.put("message", "Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultList);
        }
    }
}
