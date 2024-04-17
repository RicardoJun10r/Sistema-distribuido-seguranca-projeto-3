package app.services;

import java.io.IOException;

import microsservice.server.ReplicaLoja3;

public class Loja3 {
    public static void main(String[] args) {
        ReplicaLoja3 replicaLoja3 = new ReplicaLoja3();
        try {
            replicaLoja3.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
