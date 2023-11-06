package org.example.app;

public class Application {
    public static void main(String[] args) throws Exception {
        DIEngine diEngine = new DIEngine(ApplicationConfig.class);
        OrderService orderService = diEngine.getInstance(OrderService.class);
        OrderService orderService1 = diEngine.getInstance(OrderService.class);
        if(!diEngine.getObjectMap().isEmpty()) {
            for (Object object : diEngine.getObjectMap().keySet()) {
                System.out.println(object);
            }
        }
        System.out.println(orderService.getOrderDetails(1));
    }
}
