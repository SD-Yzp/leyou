package com.leyou.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.Nullable;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author: HuYi.Zhang
 * @create: 2018-04-24 17:20
 **/
public class JsonUtils {

    public static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    @Nullable
    public static String serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("json序列化出错：" + obj, e);
            return null;
        }
    }

    @Nullable
    public static <T> T parse(String json, Class<T> tClass) {
        try {
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Nullable
    public static <E> List<E> parseList(String json, Class<E> eClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, eClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Nullable
    public static <K, V> Map<K, V> parseMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Nullable
    public static <T> T nativeRead(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Data   //不知道什么问题，确实用不了，可能是版本吧
    static class User{
        private String name;
        private Integer age;

        /*public User(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public User() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }*/
    }

    public static void main(String[] args) {
        User user = new User();
        user.setName("zhuzhu");
        user.setAge(17);

        System.out.println(user);

        /*//序列化
        String u_json = serialize(user);
        System.out.println(u_json);

        //反序列化
        User j_user = parse(u_json, User.class);
        System.out.println(j_user.getName());

        //转成list
        String json = "[10,-10,20,5]";
        List<Integer> integerList = parseList(json, Integer.class);
        System.out.println(integerList);

        //转成map
        json = "{\"name\":\"zhuzhu\",\"age\": 17}";
        Map<String, Object> stringObjectMap = parseMap(json, String.class, Object.class);
        System.out.println(stringObjectMap);

        //复杂类型
        json = "[{\"name\":\"zhuzhu\",\"age\": 17},{\"name\":\"xiuxiu\",\"age\": 18}]";
        List<Map<String, Object>> maps = nativeRead(json, new TypeReference<List<Map<String, Object>>>() {
        });
        System.out.println(maps);*/

    }
}
