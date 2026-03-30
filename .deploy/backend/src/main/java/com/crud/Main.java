package com.crud;

import com.crud.config.AppConfig;

public class Main {
    public static void main(String[] args) {
        AppConfig app = new AppConfig();
        app.start(8080);
        System.out.println("Server running at http://localhost:8080");
        System.out.println("API available at http://localhost:8080/api/items");
    }
}
