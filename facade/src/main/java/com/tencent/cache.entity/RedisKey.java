package com.tencent;


public enum RedisKey {
    ,
    ;

    private static final String preFix = "tencent";

    public static String getCachekey() {
        return preFix + "enum";
    }


}

