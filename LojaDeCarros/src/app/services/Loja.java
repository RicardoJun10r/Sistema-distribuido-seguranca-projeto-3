package app.services;

import java.io.IOException;

import microsservice.server.LojaService;

public class Loja {
    
    public static void main(String[] args) {
        try {
            LojaService lojaService = new LojaService();
            lojaService.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
