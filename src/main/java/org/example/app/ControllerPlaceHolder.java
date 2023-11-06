package org.example.app;

import org.example.annotations.Controller;
import org.example.annotations.GET;
import org.example.annotations.Path;

@Controller(path="/controller")
public class ControllerPlaceHolder {
    @Path(path = "/path")
    @GET
    public void method()
    {
        System.out.println("get");
    }
}
