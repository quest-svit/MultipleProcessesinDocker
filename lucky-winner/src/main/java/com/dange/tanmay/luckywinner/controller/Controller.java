package com.dange.tanmay.luckywinner.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Random;

@RestController
public class Controller {
    @Autowired
    RestTemplate restTemplate;



    @GetMapping("/winner")
    public String winner(){
        int totalCount =  Integer.parseInt(getCount());

        Random random=new Random();
        int rand = random.nextInt(totalCount);
        return fetchCustomerName(rand);
    }

    private String getCount(){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        return restTemplate.exchange("http://localhost:8081/count", HttpMethod.GET, entity, String.class).getBody();

    }

    private String fetchCustomerName(int id){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        return restTemplate.exchange("http://localhost:8081/getId/" + id, HttpMethod.GET, entity, String.class).getBody();

    }

}
