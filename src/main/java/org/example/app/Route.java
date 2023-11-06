package org.example.app;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Route {
    private final Object controller;
    private final Method method;

    public Route(Object controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    public Object invokeMethod(String placeholder) {

        Class<?>[] params = method.getParameterTypes();

        if (params.length == 0) {

            try {
                return method.invoke(controller);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            //todo placeholder for request
        } else if (params.length == 1 && params[0].equals(String.class)) {

            try {
                return method.invoke(controller, placeholder);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
