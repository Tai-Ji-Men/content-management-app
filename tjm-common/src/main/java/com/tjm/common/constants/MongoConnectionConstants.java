package com.tjm.common.constants;

public class MongoConnectionConstants {
    private MongoConnectionConstants(){}

    public static final String DBNAME = ".dbname";
    public static final String USERNAME = ".username";
    public static final String PASSWORD = ".password";
    public static final String HOSTS_GROUP = ".hosts.group";
    public static final String REPORTDB_PRIMARY = ".primary";

    public static final String AUTH_TYPE_CR = "CR";
    public static final String AUTH_TYPE_X509 = "X509";

    public static enum MongoAuthenticationTypes {
        X509
    }

    public static enum MongoConnectionStatus {
        SUCCESS, FAILURE
    }
}
