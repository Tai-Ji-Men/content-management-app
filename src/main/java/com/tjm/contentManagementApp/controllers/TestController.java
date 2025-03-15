package com.tjm.contentManagementApp.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    // add some test controller method for rest api

    @GetMapping("/test/foo")
    @ResponseBody
    public String testRestAPIMethod() {
        return "testRestAPIMethod";
    }
}
