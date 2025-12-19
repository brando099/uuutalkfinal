package cn.keeponline.telegram.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Wind
 * @since 2019-06-05 20:42
 */
public final class Json2 {

    private static Logger logger = LoggerFactory.getLogger(Json2.class);

    private static ObjectMapper objectMapper;
    /**
     * 时间格式化不同的ObjectMapper
     */
    private static Map<String, ObjectMapper> objectMappers = new HashMap<>();

    static {
        objectMapper = new ObjectMapper();
        objectMapper.setTimeZone(TimeZone.getDefault());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //objectMapper.configure(DeserializationFeature.USE_LONG_FOR_INTS,true);
        //序列化时，日期的统一格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 根据时间格式化创建 ObjectMapper
     *
     * @param dateFormat 时间格式化字符串
     * @return ObjectMapper 格式化对象
     */
    private static ObjectMapper createByDateFormat(String dateFormat) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setTimeZone(TimeZone.getDefault());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //序列化时，日期的统一格式
        objectMapper.setDateFormat(new SimpleDateFormat(dateFormat));
        return objectMapper;
    }

    /**
     * 根据时间格式化获取ObjectMapper
     *
     * @param dateFormat 时间格式化字符串
     * @return ObjectMapper 格式化对象
     */
    private static ObjectMapper getByDateFormat(String dateFormat) {
        ObjectMapper objectMapper = objectMappers.get(dateFormat);
        if (null == objectMapper) {
            objectMapper = createByDateFormat(dateFormat);
            objectMappers.put(dateFormat, objectMapper);
        }
        return objectMapper;
    }

    /**
     * 根据时间格式化格式获取json
     *
     * @param object     需要json的对象
     * @param dateFormat 时间格式化字符串
     * @return json化后的字符串
     */
    public static String toJson(Object object, String dateFormat) {
        if (Strings.isNullOrEmpty(dateFormat)) {
            return toJson(object);
        }
        ObjectMapper objectMapper = getByDateFormat(dateFormat);
        if (null == objectMapper) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Throwable e) {
            logger.error("对象转json异常，dateFormat={}，obj={}", dateFormat, object);
            throw new RuntimeException("对象序列化json异常", e);
        }
    }

    /**
     * 对象转json
     *
     * @param object 被序列化的对象
     * @return json 序列化好的json字符串
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Throwable e) {
            logger.error("对象序列化json异常，obj={}", object);
            throw new RuntimeException("序列化Json异常", e);
        }
    }


    /**
     * json转对象
     *
     * @param json     json string
     * @param classOfT class的类型
     * @return 反序列化好的对象
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return objectMapper.readValue(json, classOfT);
        } catch (Throwable e) {
            logger.error("json转对象异常,json={}", json);
            throw new RuntimeException("json转对象异常,json = " + json, e);
        }
    }

    /**
     * json转对象
     *
     * @param json      json string
     * @param classOfT  class的类型
     * @param classBody classBody
     * @return 反序列化好的对象
     */
    public static <T> T fromJson(String json, Class<T> classOfT, Class<T> classBody) {
        try {
            objectMapper.convertValue(json, classBody);
            return objectMapper.readValue(json, classOfT);
        } catch (Throwable e) {
            logger.error("json转对象异常,json={}", json);
            throw new RuntimeException("json转对象异常,json = " + json, e);
        }
    }

    /**
     * linkedMap转对象
     *
     * @param map      map
     * @param classOfT class的类型
     * @return 反序列化好的对象
     */
    public static <T> T convertValue(Object map, Class<T> classOfT) {
        try {
            return objectMapper.convertValue(map, classOfT);
        } catch (Throwable e) {
            logger.error("map转对象异常,map={}", map);
            throw new RuntimeException("json转对象异常,map = " + map, e);
        }
    }


    /**
     * 转数组
     *
     * @param json json string
     * @return List
     */
    public static <T> List<T> jsonToList(String json, Class<T> clazz) {
        return jsonToListByObjectMapper(objectMapper, json, clazz);
    }

    /**
     * JSON字符串转换为Java Array<String>
     *
     * @param <T>        转换泛型
     * @param jsonString JSON字符串
     * @param tr         需要转换的对象类型
     * @return Java泛型对象
     */
    public static <T> T jsonToList2(String jsonString, TypeReference<T> tr) {
        if (jsonString != null && !("".equals(jsonString))) {
            try {
                return (T) (tr.getType().equals(String.class) ? jsonString : objectMapper.readValue(jsonString, tr));
            } catch (Exception e) {
                logger.warn("json error:" + e.getMessage());
            }
        }
        return null;
    }

    /**
     * @param json       代转字符串
     * @param clazz      类
     * @param timeFomart 格式化时间字符串
     * @param <T>        返回类型
     * @return 结果数组
     */
    public static <T> List<T> jsonToList(String json, Class<T> clazz, String timeFomart) {
        ObjectMapper objectMapper = getByDateFormat(timeFomart);
        return jsonToListByObjectMapper(objectMapper, json, clazz);
    }

    /**
     * 根据传入的ObjectMapper进行json化，不同的ObjectMapper 有着不同的格式化配置
     *
     * @param objectMapper 格式化工具
     * @param json         待转的字符串
     * @param clazz        类
     * @param <T>          类型
     * @return 转换后的结果数组
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> jsonToListByObjectMapper(ObjectMapper objectMapper, String json, Class<T> clazz) {
        try {
            List<LinkedHashMap> linkedHashMaps = objectMapper.readValue(json, new TypeReference<List<LinkedHashMap>>() {

            });
            List<T> arrayList = new ArrayList();
            for (LinkedHashMap linkedHashMap : linkedHashMaps) {
                arrayList.add(objectMapper.readValue(Json2.toJson(linkedHashMap), clazz));
            }
            return arrayList;
        } catch (Throwable e) {
            logger.error("jsonToListByObjectMapper 转化异常，json={}", json);
            throw new RuntimeException("jsonToListByObjectMapper 转化异常", e);
        }
    }

    /**
     * 判断待解析的字符串是否是数组
     *
     * @param json json字符串
     * @return 判断结果
     */
    public static boolean isArray(String json) {
        try {
            objectMapper.readValue(json, new TypeReference<List<LinkedHashMap>>() {
            });
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}


