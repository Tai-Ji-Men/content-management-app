package com.tjm.mongo.utils;

import java.util.Map;

public abstract class FuncType {
    private final String name;

    public FuncType(String name) {
        this.name = name;
    }

    public abstract void call(Map<String, Object> params) throws Exception;

    public String getName() {
        return name;
    }
}
