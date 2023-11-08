package org.example.framework;



import org.example.app.DIEngine;
import org.example.app.Route;
import org.example.framework.request.Header;
import org.example.framework.request.Helper;
import org.example.framework.request.Request;
import org.example.framework.request.enums.Method;
import org.example.framework.request.exceptions.RequestNotValidException;
import org.example.framework.response.JsonResponse;
import org.example.framework.response.Response;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerThread implements Runnable{

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ServerThread(Socket socket){
        this.socket = socket;

        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {

            //generates
            Request request = this.generateRequest();
            if(request == null) {
                in.close();
                out.close();
                socket.close();
                return;
            }


            // Response example
            //Map<String, Object> responseMap = new HashMap<>();
            //responseMap.put("route_location", request.getLocation());
            //responseMap.put("route_method", request.getMethod().toString());
            //responseMap.put("parameters", request.getParameters());
            //Response response = new JsonResponse(responseMap);
            String path = request.getMethod() + ":" + request.getLocation().split("\\?")[0];
            Route route = DIEngine.getInstance().getRoute(path);
            Object response = null;

            if (route != null)
                response = route.invokeMethod(request);

            if (response instanceof Response)
                out.println(((Response) response).render());

            in.close();
            out.close();
            socket.close();

        } catch (IOException | RequestNotValidException e) {
            e.printStackTrace();
        }
    }

    //processes the request from the user and generates a Request class
    private Request generateRequest() throws IOException, RequestNotValidException {
        String command = in.readLine();
        if(command == null) {
            return null;
        }

        String[] actionRow = command.split(" ");
        Method method = Method.valueOf(actionRow[0]);
        String route = actionRow[1];
        Header header = new Header();
        HashMap<String, String> parameters = Helper.getParametersFromRoute(route);

        do {
            command = in.readLine();
            String[] headerRow = command.split(": ");
            if(headerRow.length == 2) {
                header.add(headerRow[0], headerRow[1]);
            }
        } while(!command.trim().equals(""));

        if(method.equals(Method.POST)) {
            int contentLength = Integer.parseInt(header.get("content-length"));
            char[] buff = new char[contentLength];
            in.read(buff, 0, contentLength);
            String parametersString = new String(buff);

            HashMap<String, String> postParameters = Helper.getParametersFromString(parametersString);
            for (String parameterName : postParameters.keySet()) {
                parameters.put(parameterName, postParameters.get(parameterName));
            }
        }

        Request request = new Request(method, route, header, parameters);

        return request;
    }
}
