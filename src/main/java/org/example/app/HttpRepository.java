package org.example.app;

import java.util.ArrayList;
import java.util.List;

public class HttpRepository {
    private List<String> list;

    public HttpRepository(){
        list = new ArrayList<>();
        list.add("GET");
        list.add("POST");
        list.add("DELETE");
    }

    public List<String> getList() {
        return list;
    }
}
