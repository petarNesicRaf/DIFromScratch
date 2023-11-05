package org.example.app;

import org.example.annotations.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DIEngine {
    Map<Class<?>, Object> objectMap = new HashMap<>();

    DIEngine(Class<?> clazz)
    {
        initializeEngine(clazz);
    }

    private void initializeEngine(Class<?> clazz)
    {
        if(!clazz.isAnnotationPresent(Configuration.class))
        {
            throw new RuntimeException("Please provide a valid configuration");
        }
        ComponentScan componentScan = clazz.getAnnotation(ComponentScan.class);
        String packageValue = componentScan.value();
        //brise duplikate klasa
        Set<Class<?>> classes = findClasses(packageValue);

        for (Class<?> loadingClass: classes)
        {
            try{
                if(loadingClass.isAnnotationPresent(Component.class) || loadingClass.isAnnotationPresent(Bean.class))
                {
                    Constructor<?> constructor = loadingClass.getDeclaredConstructor();
                    Object newInstance = constructor.newInstance();
                    objectMap.put(loadingClass, newInstance);
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    //vraca objekat klase koji je prosledjen i injectuje sve atribute te klase
    public <T> T getInstance(Class<T> clazz) throws Exception{

        if(clazz.isAnnotationPresent(Bean.class))
        {
            Bean bean = clazz.getAnnotation(Bean.class);
            //kreira novi bean objekat ukoliko bean nije parametrizovan kao singleton
            if(!bean.scope())
            {
                Constructor<?> constructor = clazz.getConstructor();
                T objectPrototype = (T)constructor.newInstance();

                Field[] declaredFields = clazz.getDeclaredFields();
                injectAnnotatedFields(objectPrototype, declaredFields);
                return objectPrototype;
            }
        }

        T object = (T)objectMap.get(clazz);

        Field[] declaredFields = clazz.getDeclaredFields();
        injectAnnotatedFields(object, declaredFields);

        return object;
    }

    //prolazi kroz sve atribute koji su anotirani i rekurzivno injectuje
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
                Object innerObject = objectMap.get(type);

                field.set(object, innerObject);
                //rekurzivan prolaz kroz klasu atributa koji je oznacen sa @AutoWired
                injectAnnotatedFields(innerObject, type.getDeclaredFields());
            }
        }
    }

    private Set<Class<?>> findClasses(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    private Class<?> getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
