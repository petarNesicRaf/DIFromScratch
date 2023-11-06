package org.example.app;

import org.example.annotations.Qualifier;

import java.util.Map;
import java.util.Set;

public class DIContainer {

    private static DIContainer instance=null;
    private Map<String, Class<?>> qualifierClasses;

    private DIContainer(){

    }
    public static DIContainer getInstance()
    {
        if(instance==null) instance = new DIContainer();

        return instance;
    }
    public void addQualifiers(Set<Class<?>> qualifiers) {

        for (Class<?> q : qualifiers) {

            if (q.isAnnotationPresent(Qualifier.class)) {

                String value = q.getAnnotation(Qualifier.class).value();

                if (find(value) != null)
                    throw new RuntimeException("Qualifier already exists");

                this.qualifierClasses.put(value, q);
            }
        }
    }

    public Class<?> find(String key) {
        return qualifierClasses.get(key);
    }

}
