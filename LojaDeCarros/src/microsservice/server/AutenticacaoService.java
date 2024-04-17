package microsservice.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import util.ClientSocket;

public class AutenticacaoService {

    public final int PORTA = 1050;

    public final int FIREWALL_PORTA = 10101;

    public final int DATABASE_PORTA = 6156;

    private final String ENDERECO_SERVER = "localhost";

    private ServerSocket serverSocket;

    private int BOSS_PORTA = 50000;

    public AutenticacaoService(){}

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORTA);
        System.out.println("Iniciando serviço de autenticação na porta = " + PORTA);
        clientConnectionLoop();
    }

    private void clientConnectionLoop() throws IOException {
        while (true) {
            ClientSocket clientSocket = new ClientSocket(this.serverSocket.accept());
            new Thread(() -> {
                try {
                    clientMessageLoop(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void clientMessageLoop(ClientSocket clientSocket) throws IOException {
        String mensagem;
        try {
            while ((mensagem = clientSocket.getMessage()) != null) {
                String[] msg = mensagem.split(";");
                String request;
                if (msg[0].equals("response")) {
                    if (msg[1].equals("login")) {
                        sendToFirewall("autenticar;servico;" + msg[1] + ";" + msg[2] + ";" + msg[3]);
                    } else if(msg[1].equals("boss")){
                        sendToBoss(msg[2]);
                    } else if (msg[1].equals("criado")) {
                        sendToFirewall("autenticar;servico;criado;" + msg[2]);
                    } else {
                        System.out.println("Erro[AutenticacaoService]: " + mensagem);
                    }
                } else {
                    switch (msg[1]) {
                        case "1": {
                            // AUTENTICAR
                            System.out.println(
                                    "[1] Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                            request = isAdmin(msg[0]) + ";" + "select;" + msg[2] + ";" + msg[3] + ";" + msg[4];
                            sendToDB(request);
                            break;
                        }
                        case "2": {
                            // CRIAR CONTA USUARIO
                            System.out.println(
                                    "[2] Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                            request = isAdmin(msg[2]) + ";" + "insert;" + msg[4] + ";" + msg[5] + ";" + msg[6];
                            sendToDB(request);
                            break;
                        }
                        default:
                            System.out.println(
                                    "Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                            break;
                    }
                }
            }
        } finally {
            clientSocket.close();
        }
    }

    private String isAdmin(String admin){
        if(Boolean.parseBoolean(admin)){
            return "funcionario";
        } else {
            return "cliente";
        }
    }

    private void sendToFirewall(String mensagem){
        ClientSocket sendFirewall;
        try {
            sendFirewall = new ClientSocket(new Socket("localhost", FIREWALL_PORTA));
            sendFirewall.sendMessage(this.ENDERECO_SERVER + ";" + this.PORTA + ";" + 1042 + ";" + mensagem);
            sendFirewall.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToBoss(String req){
        ClientSocket sendBoss;
        try {
            sendBoss = new ClientSocket(new Socket(ENDERECO_SERVER, BOSS_PORTA));
            sendBoss.sendMessage(req);
            sendBoss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToDB(String req){
        ClientSocket send_database;
        try {
            send_database = new ClientSocket(new Socket("localhost", DATABASE_PORTA));
            send_database.sendMessage(req);
            send_database.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
