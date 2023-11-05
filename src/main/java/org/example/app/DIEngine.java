package org.example.app;

import org.example.annotations.AutoWired;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class DIEngine {
    Map<Class<?>, Object> objectMap = new HashMap<>();

    //vraca objekat klase koji je prosledjen i injectuje sve atribute te klase
    public <T> T getInstance(Class<T> clazz) throws Exception{
        Constructor<?> constructor = clazz.getConstructor();
        T object = (T)constructor.newInstance();

        Field[] declaredFields = clazz.getDeclaredFields();
        injectAnnotatedFields(object, declaredFields);

        return object;
    }

    //
    private <T> void injectAnnotatedFields(T object, Field[] declaredFields) throws Exception{
        for (Field field : declaredFields)
        {
            if(field.isAnnotationPresent(AutoWired.class))
            {
                AutoWired autoWired = field.getAnnotation(AutoWired.class);

                if(autoWired.verbose()) {
                    LocalDateTime currDate = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                    System.out.println("Initialized " + field.getType() + " " + field.getName() + " in " + field.getDeclaringClass() + " on " + currDate.format(formatter)+ " with " + field.hashCode());
                }

                field.setAccessible(true);
                Class<?> type = field.getType();
                Object innerObject = type.getDeclaredConstructor().newInstance();

                field.set(object, innerObject);
                //rekurzivan prolaz kroz atribut koji je oznacen sa @AutoWired
                injectAnnotatedFields(innerObject, type.getDeclaredFields());
            }
        }
    }

}
