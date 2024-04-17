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

    private final int FIREWALL_PORTA = 10101;

    private final List<ClientSocket> USUARIOS = new LinkedList<>();

    public Backdoor() {
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORTA);
        System.out.println("Iniciando Backdoor na porta = " + PORTA);
        malware();
        mainLoop();
    }

    private void mainLoop() throws IOException {
        while (true) {
            ClientSocket clientSocket = new ClientSocket(this.serverSocket.accept());
            USUARIOS.add(clientSocket);
            new Thread(() -> {
                try {
                    backdoorLoop(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void backdoorLoop(ClientSocket clientSocket) throws IOException {
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
        ClientSocket sendFirewall;
        try {
            sendFirewall = new ClientSocket(new Socket(ENDERECO_SERVER, FIREWALL_PORTA));
            sendFirewall.sendMessage(ENDERECO_SERVER + ";" + 1048 + ";" + 1048);
            sendFirewall.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
