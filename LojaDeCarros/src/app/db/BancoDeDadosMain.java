package app.db;

import java.io.IOException;

import db.BancoDeDados;

public class BancoDeDadosMain {
    public static void main(String[] args) {
        BancoDeDados bancoDeDados = new BancoDeDados();
        try {
            bancoDeDados.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
