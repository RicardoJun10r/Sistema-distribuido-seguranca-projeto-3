package app.gateway;

import java.io.IOException;

import microsservice.gateway.ReplicaGateway;

public class ReplicaGatewayMain {
    public static void main(String[] args) {
        ReplicaGateway replicaGatewayMain = new ReplicaGateway();
        try {
            replicaGatewayMain.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
