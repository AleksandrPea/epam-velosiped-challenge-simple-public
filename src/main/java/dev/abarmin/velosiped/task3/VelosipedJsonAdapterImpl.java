package dev.abarmin.velosiped.task3;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class VelosipedJsonAdapterImpl implements VelosipedJsonAdapter{
    @Override
    public <T> T parse(String json, Class<T> targetClass) {
        String str = json.substring(json.indexOf("{") + 1).replace("}", "").trim();
        String[] fieldsStr = str.split(",");
        Map<String, String> fieldsMap = Arrays.stream(fieldsStr)
                .map(field -> field.trim().split("\\:"))
                .collect(Collectors.toMap(field -> field[0]
                        .replace("\"", "").trim(), field -> field[1].trim()));
        T instance;
        try {
            instance = targetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Field[] fields = targetClass.getDeclaredFields();
        Arrays.stream(fields)
                .forEach(field -> {
                    String fieldValue = fieldsMap.get(field.getName());
                    if (fieldValue == null) {
                        return;
                    }
                    field.setAccessible(true);
                    try {
                        field.set(instance, mapValue(field.getType(), fieldValue)); // quick hack
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
        return instance;
    }

    private Object mapValue(Class<?> valueClass, String value) {
        if (valueClass.equals(int.class)) {
            return Integer.parseInt(value);
        } else {
            throw new RuntimeException("Unsupported class");
        }
    }

    @Override
    public String writeAsJson(Object instance) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        Class<?> instanceClass = instance.getClass();
        Field[] fields = instanceClass.getDeclaredFields();
        String fieldsStr = Arrays.stream(fields)
                .map(field -> {
                    StringBuilder fieldStr = new StringBuilder();
                    field.setAccessible(true);
                    String value;
                    try {
                        value = field.get(instance).toString();  // quick hack
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    fieldStr.append("\"").append(field.getName()).append("\"");
                    fieldStr.append(":");
                    fieldStr.append(value);
                    return fieldStr;
                }).collect(Collectors.joining(","));
        json.append(fieldsStr);
        json.append("}");
        return json.toString();
    }
}
