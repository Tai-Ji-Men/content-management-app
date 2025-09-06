package com.tjm.tjmwebapp.controllers;

import com.tjm.tjmwebapp.services.TJMNewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/tjm/api/v1")
public class TJMNewsController {

    private static Logger LOGGER = LoggerFactory.getLogger(TJMNewsController.class);

    @Autowired
    TJMNewsService tjmNewsService;

    @GetMapping("/news")
    public ResponseEntity<Map<String, Object>> getNews() {
        Map<String, Object> payload = tjmNewsService.getNewsPagePayload();
        return ResponseEntity.ok()
                // cache publicly for 60s; tune as desired
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(payload);
    }

    @GetMapping("/news/articles/{slug}")
    public ResponseEntity<?> getArticle(@PathVariable String slug) {
        LOGGER.info("GET /tjm/api/v1/news/articles/{}", slug);
        return tjmNewsService.getArticleBySlug(slug);
    }


}
