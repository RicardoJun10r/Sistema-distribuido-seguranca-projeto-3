package app.gateway;

import java.io.IOException;

import microsservice.gateway.Gateway;

public class GatewayMain {
    public static void main(String[] args) {
        Gateway gateway = new Gateway();
        try {
            gateway.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
