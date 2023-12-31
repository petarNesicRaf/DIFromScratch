package org.example.app;

import org.example.annotations.AutoWired;
import org.example.annotations.Component;
import org.example.annotations.Service;

@Component
public class HttpService {
    @AutoWired(verbose = true)
    HttpRepository httpRepository;

    public void printRepository()
    {
        System.out.println(this.httpRepository.getList().toString());
    }

    public HttpRepository getHttpRepository() {
        return httpRepository;
    }
}
