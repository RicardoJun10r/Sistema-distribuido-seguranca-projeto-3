package app.services;

import java.io.IOException;

import microsservice.server.AutenticacaoService;

public class AutenticarMain {
    public static void main(String[] args) {
        AutenticacaoService autenticacaoService = new AutenticacaoService();
        try {
            autenticacaoService.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
