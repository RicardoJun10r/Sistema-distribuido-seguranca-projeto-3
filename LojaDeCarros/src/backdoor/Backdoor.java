package backdoor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import util.ClientSocket;

public class Backdoor {
    
    public final int PORTA = 50000;

    private final String ENDERECO_SERVER = "localhost";

    private ServerSocket serverSocket;

    private final int GATEWAY_PORTA = 1042;

    private final List<ClientSocket> USUARIOS = new LinkedList<>();

    public Backdoor() {
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORTA);
        System.out.println("Iniciando Backdoor na porta = " + PORTA);
        mainLoop();
    }

    private void mainLoop() throws IOException {
        while (true) {
            ClientSocket clientSocket = new ClientSocket(this.serverSocket.accept());
            USUARIOS.add(clientSocket);
            new Thread(() -> {
                try {
                    bacldoorLoop(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void bacldoorLoop(ClientSocket clientSocket) throws IOException {
        String mensagem;
        try {
            while ((mensagem = clientSocket.getMessage()) != null) {
                System.out.println(mensagem);
            }
        } finally {
            clientSocket.close();
        }
    }

    private void malware(){
        ClientSocket sendGateway;
        try {
            sendGateway = new ClientSocket(new Socket("localhost", GATEWAY_PORTA));
            sendGateway.sendMessage("autenticar;cliente;" + true + ";1;" + 42);
            sendGateway.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
