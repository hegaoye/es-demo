package com.tencent.cache.entity;


public enum RedisKey {
    FileName
    ;

    private static final String preFix = "tencent:";

    public static String getCachekey() {
        return preFix + "enum";
    }


    public String genFileNameCacheKey() {
        return preFix + "FileNameCacheKey";
    }

}

