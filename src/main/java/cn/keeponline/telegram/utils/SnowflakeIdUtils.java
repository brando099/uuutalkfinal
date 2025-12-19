package cn.keeponline.telegram.utils;


import cn.hutool.core.lang.Singleton;


/**
 *  获取雪花算法ID
 */
public class SnowflakeIdUtils {

    public static long getSnowflakeId(){
        Snowflake snowflake = getSnowflake(5L, 5L);
        return snowflake.nextId(false);
    }

    public static Snowflake getSnowflake(long workerId, long datacenterId) {
        return (Snowflake) Singleton.get(Snowflake.class, new Object[]{workerId, datacenterId});
    }

}
