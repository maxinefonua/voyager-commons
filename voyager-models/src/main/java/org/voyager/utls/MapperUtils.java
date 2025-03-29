package org.voyager.utls;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.ParameterizedType;

public class MapperUtils<T> {
    private Class<T> type;
    public MapperUtils(Class<T> type){
        this.type = type;
    }

    private ObjectMapper om = new ObjectMapper();
    public String mapToJson(T object) {
        try {
            return om.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public T mapFromJson(String jsonString) {
        try {
            return om.readValue(jsonString, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
