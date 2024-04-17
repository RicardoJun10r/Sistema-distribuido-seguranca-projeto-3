package app.services;

import java.io.IOException;

import microsservice.server.ReplicaLoja2;

public class Loja2 {
    public static void main(String[] args) {
        ReplicaLoja2 replicaLoja2 = new ReplicaLoja2();
        try {
            replicaLoja2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
