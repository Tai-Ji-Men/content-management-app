package com.tjm.mongo.models;

import com.mongodb.WriteConcern;

public enum TJMWriteConcern {

    ACKNOWLEDGED, MAJORITY, W2, W1;

    // for aggregates
    public static WriteConcern toWriteConcern(boolean isSingleAckOk) {
        if (isSingleAckOk) {
            return WriteConcern.W1;
        }
        return WriteConcern.W2;
    }



}
