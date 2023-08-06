package com.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 10, HttpClient.newHttpClient(), new ObjectMapper().registerModule(new JavaTimeModule()), new Timer());

        CrptApi.ProductMadeInRussiaToSendInSalesDocument product = new CrptApi.ProductMadeInRussiaToSendInSalesDocument();
        product.setProductionDate(LocalDate.now());
        String token = "123123123123";

        //TEST
        int requestsQuantity = 50;
        List<Thread> list = new ArrayList<>(requestsQuantity);
        for (int i = 0; i < requestsQuantity; i++) {
            Thread thread = new Thread(() -> crptApi.createDocumentToSendInSalesProductMadeInRussia(product, token));
            list.add(thread);
        }
        list.forEach(thread -> thread.start());
    }
}
