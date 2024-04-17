package app.db;

import java.io.IOException;

import db.ReplicaBancoDeDados3;

public class Banco3 {
    public static void main(String[] args) {
        ReplicaBancoDeDados3 replicaBancoDeDados3 = new ReplicaBancoDeDados3();
        try {
            replicaBancoDeDados3.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
