package org.example.app;

import org.example.annotations.AutoWired;
import org.example.annotations.Controller;
import org.example.annotations.GET;
import org.example.annotations.Path;

//@Controller(path="/controller")
public class ControllerPlaceHolder {
  //  @AutoWired(verbose = true)
    private OrderRepository orderRepository;
 //   @Path(path = "/path")
 //   @GET
    public void method()
    {
        System.out.println("get invoke");
    }
}
