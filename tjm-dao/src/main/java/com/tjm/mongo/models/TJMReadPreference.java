package com.tjm.mongo.models;

import com.mongodb.ReadPreference;
import com.mongodb.TagSet;

import java.util.List;

public enum TJMReadPreference {

    primary, primaryPreferred, secondary, secondaryPreferred, nearest;

    public ReadPreference toReadPreference() {
        return ReadPreference.valueOf(this.name());
    }
    public ReadPreference toReadPreference(List<TagSet> tagSetList) {
        return ReadPreference.valueOf(this.name(),tagSetList);
    }

}
