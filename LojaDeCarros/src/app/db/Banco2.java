package app.db;

import java.io.IOException;

import db.ReplicaBancoDeDados2;

public class Banco2 {
    public static void main(String[] args) {
        ReplicaBancoDeDados2 replicaBancoDeDados2 = new ReplicaBancoDeDados2();
        try {
            replicaBancoDeDados2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
