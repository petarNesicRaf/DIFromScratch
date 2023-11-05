package org.example.app;

public class Application {
    public static void main(String[] args) throws Exception {
        DIEngine diEngine = new DIEngine(ApplicationConfig.class);
        OrderService orderService = diEngine.getInstance(OrderService.class);
        System.out.println(orderService.getOrderDetails(1));
    }
}
