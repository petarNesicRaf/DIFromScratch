package org.example.app;

import org.example.annotations.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DIEngine {
    private Map<Class<?>, Object> objectMap = new HashMap<>();
    private Set<Class<?>> controllerSet = new HashSet<>();
    private Map<String, Route> routesMap = new HashMap<>();

    private Set<Class<?>> qualifiersSet = new HashSet<>();
    private static DIEngine instance=null;

    private DIEngine(Class<?> clazz)
    {
            initializeEngine(clazz);
            DIContainer.getInstance().addQualifiers(qualifiersSet);
    }
    public static DIEngine getInstance()
    {
        if(instance==null) instance = new DIEngine(ApplicationConfig.class);

        return instance;
    }

    //inicijalizacija engine-a citanje klasa iz paketa i procesuiranje anotacija
    private void initializeEngine(Class<?> clazz)
    {
        if(!clazz.isAnnotationPresent(Configuration.class))
        {
            throw new RuntimeException("Please provide a valid configuration");
        }
        ComponentScan componentScan = clazz.getAnnotation(ComponentScan.class);
        String packageValue = componentScan.value();
        //izvlaci klase iz paketa
        Set<Class<?>> classes = findClasses(packageValue);

        for (Class<?> loadingClass: classes)
        {
            try{
                if(loadingClass.isAnnotationPresent(Component.class) || loadingClass.isAnnotationPresent(Bean.class)
                        || loadingClass.isAnnotationPresent(Service.class) || loadingClass.isAnnotationPresent(Controller.class) || loadingClass.isAnnotationPresent(Qualifier.class))
                {
                    //ukoliko je klasa anotirana sa Qualifier
                    if(loadingClass.isAnnotationPresent(Qualifier.class))
                    {
                        this.qualifiersSet.add(loadingClass);
                    }

                    if(loadingClass.isAnnotationPresent(Controller.class))
                    {
                        this.controllerSet.add(loadingClass);
                    }else {
                        //mozda treba ovde da se zove getInstance da se ne zove u main

                        if(loadingClass.isAnnotationPresent(Service.class)){
                            Constructor<?> constructor = loadingClass.getDeclaredConstructor();
                            Object newInstance = constructor.newInstance();
                            if(!objectMap.containsKey(loadingClass)) {
                                objectMap.put(loadingClass, newInstance);
                                injectAnnotatedFields(newInstance, loadingClass.getDeclaredFields());
                            }
                        }
                        else if(loadingClass.isAnnotationPresent(Bean.class))
                        {
                            if(loadingClass.getAnnotation(Bean.class).scope())
                            {
                                Constructor<?> constructor = loadingClass.getDeclaredConstructor();
                                Object newInstance = constructor.newInstance();
                                if(!objectMap.containsKey(loadingClass)) {
                                    objectMap.put(loadingClass, newInstance);
                                    injectAnnotatedFields(newInstance, loadingClass.getDeclaredFields());
                                }
                            }
                        }
                    }
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if(!this.controllerSet.isEmpty())
            insertRoutes(controllerSet);
    }

    //prolazi kroz sve atribute koji su anotirani i rekurzivno injectuje
    private <T> void injectAnnotatedFields(T object, Field[] declaredFields) throws Exception{
        for (Field field : declaredFields)
        {
            if(field.isAnnotationPresent(AutoWired.class)) {
                AutoWired autoWired = field.getAnnotation(AutoWired.class);

                if (autoWired.verbose()) {
                    LocalDateTime currDate = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                    System.out.println("Initialized " + field.getType() + " " + field.getName() + " in " + field.getDeclaringClass() + " on " + currDate.format(formatter) + " with " + field.hashCode());
                }
                //ukoliko je atribut anotiran sa qualifier uz autowired
                if(field.isAnnotationPresent(Qualifier.class))
                {
                    this.qualifiersSet.add(field.getType());
                }

                field.setAccessible(true);
                Class<?> type = field.getType();
                Object innerObject = objectMap.get(type);
                //rekurzivan prolaz kroz klasu atributa koji je oznacen sa @AutoWired
                if (innerObject == null){
                    //ukoliko objekat nije singleton onda nije u mapi, kreira se novi objekat
                    //prolazi se kroz anotacije tog novog objekta
                    Constructor<?> constructor = type.getDeclaredConstructor();
                    Object innerObject1 = constructor.newInstance();
                    field.set(object,innerObject1);
                    injectAnnotatedFields(innerObject1, type.getDeclaredFields());
                }else{
                    //ukoliko objekat jeste singleton nalazi se u mapi
                    field.set(object, innerObject);
                    injectAnnotatedFields(innerObject, type.getDeclaredFields());
                }
            }
        }
    }

    private void insertRoutes(Set<Class<?>> controllerSet)
    {
        for (Class<?> contr : controllerSet) {
            try {
                //instanciranje svakog kontrolera i dodavanje njegovih anotacija
                Constructor<?>constructor = contr.getDeclaredConstructor();
                Object newInstance = constructor.newInstance();
                //rekurzivno dodavanje anotacija kontroleru
                injectAnnotatedFields(newInstance, contr.getDeclaredFields());

                Method[] methods = contr.getDeclaredMethods();

                //prolaz kroz metode, kreiranje ruta i dodavanje u mapu controller-ruta string ruta
                for (Method method : methods) {

                    String route = "";
                    if (contr.isAnnotationPresent(Controller.class)) {
                        if (method.isAnnotationPresent(Path.class)) {

                            if (method.isAnnotationPresent(GET.class)) {
                                route = "GET:" + contr.getAnnotation(Controller.class).path()  + method.getAnnotation(Path.class).path();
                                System.out.println(route);
                            } else if (method.isAnnotationPresent(POST.class)) {
                                route = "POST:" + contr.getAnnotation(Controller.class).path()  + method.getAnnotation(Path.class).path();
                                System.out.println(route);
                            }

                            Route controllerRoute = new Route(newInstance, method);
                            this.routesMap.put(route, controllerRoute);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public Route getRoute(String path)
    {
        return this.routesMap.get(path);
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

    public Object get(Class<?> clazz)
    {
        // ukoliko objekat nije singleton
        try {
            if (clazz.isAnnotationPresent(Component.class)) {
                Constructor constructor = clazz.getDeclaredConstructor();
                Object object = constructor.newInstance();
                injectAnnotatedFields(object, clazz.getDeclaredFields());
                return object;
            } else if (clazz.isAnnotationPresent(Bean.class) && !clazz.getAnnotation(Bean.class).scope()) {
                Constructor constructor = clazz.getDeclaredConstructor();
                Object object = constructor.newInstance();
                injectAnnotatedFields(object, clazz.getDeclaredFields());
                return object;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this.objectMap.get(clazz);
    }
}
