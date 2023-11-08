package org.example.app;

import org.example.annotations.*;
import org.example.framework.request.Request;
import org.example.framework.response.JsonResponse;
import org.example.framework.response.Response;

import java.util.HashMap;
import java.util.Map;

@Controller(path="/controller")
public class DIController {

    @Path(path = "/get-path")
    @GET
    public Response getMethod(Request request)
    {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("route_location", request.getLocation());
        responseMap.put("route_method", request.getMethod().toString());
        responseMap.put("parameters", request.getParameters());
        Response response = new JsonResponse(responseMap);
        return response;
    }
    @Path(path="/post-path")
    @POST
    public Response postMethod(Request request)
    {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("route_location", request.getLocation());
        responseMap.put("route_method", request.getMethod().toString());
        responseMap.put("parameters", request.getParameters());
        Response response = new JsonResponse(responseMap);
        return response;
    }
}
