package com.argos.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DummyService {

    @Autowired
    private RestTemplate restTemplate; // THIS IS THE VIOLATION

    public void doNothing() {
        // Just a dummy method
    }
}