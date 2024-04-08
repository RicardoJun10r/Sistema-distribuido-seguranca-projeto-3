package app;

import java.io.IOException;

import client.UserInterface;

public class UserMain {
    public static void main(String[] args) {
        try {
            /**
             * true --> ENTRA COMO FUNCIONARIO
             * false --> ENTRA COMO USUÁRIO NORMAL
             */
            UserInterface usuarios = new UserInterface(true);
            usuarios.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
