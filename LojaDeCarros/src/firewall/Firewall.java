package firewall;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import util.ClientSocket;

public class Firewall {

    public final int PORTA = 10101;

    private final String ENDERECO_SERVER = "localhost";

    private ServerSocket serverSocket;

    private final int AUTENTICACAO_PORTA = 1050;

    private final int LOJA_PORTA = 1060;

    private final int GATEWAY_PORTA = 1042;

    private final Vector<ClientSocket> servicos = new Vector<>();

    private final List<ClientSocket> USUARIOS = new LinkedList<>();

    public Firewall() {
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORTA);
        System.out.println("Iniciando FIREWALL na porta = " + PORTA);
        // this.servicos.add(new ClientSocket(new Socket(ENDERECO_SERVER,
        // GATEWAY_PORTA)));
        // System.out.println("Conectado ao gateway ...");
        this.servicos.add(new ClientSocket(new Socket(ENDERECO_SERVER, AUTENTICACAO_PORTA)));
        System.out.println("Conectado ao serviço de autenticação ...");
        this.servicos.add(new ClientSocket(new Socket(ENDERECO_SERVER, LOJA_PORTA)));
        System.out.println("Conectado ao serviço da loja ...");
        mainLoop();
    }

    private void mainLoop() throws IOException {
        while (true) {
            ClientSocket clientSocket = new ClientSocket(this.serverSocket.accept());
            USUARIOS.add(clientSocket);
            new Thread(() -> {
                try {
                    firewallLoop(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private Boolean politicasSeguranca(String endereco, int porta) {
        System.out.println("Serviço tentando entrar: endereço [ " + endereco + " ] porta [ " + porta + " ]");
        if (endereco.equals("localhost")) {
            switch (porta) {
                case GATEWAY_PORTA:
                    System.out.println("GATEWAY ENTROU");
                    return true;
                case AUTENTICACAO_PORTA:
                    System.out.println("AUTENTICACAO ENTROU");
                    return true;
                case LOJA_PORTA:
                    System.out.println("LOJA ENTROU");
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    private void firewallLoop(ClientSocket clientSocket) throws IOException {
        String mensagem;
        try {
            while ((mensagem = clientSocket.getMessage()) != null) {
                String[] msg = mensagem.split(";");
                if (politicasSeguranca(msg[0], Integer.parseInt(msg[1]))) {
                    System.out.println("SERVICO ENTROU");
                    String req = request(msg);
                    System.out.println("Requisição: " + req);
                    switch (Integer.parseInt(msg[2])) {
                        case AUTENTICACAO_PORTA:
                            System.out.println("sendAutenticar()");
                            sendAutenticar(req);
                            break;
                        case LOJA_PORTA:
                            System.out.println("sendLoja()");
                            sendLoja(req);
                            break;
                        case GATEWAY_PORTA:
                            System.out.println("sendToGateway()");
                            sendToGateway(req);
                            break;
                        default:
                            System.out.println("Erro [ Firewall ]: politicasSeguranca-switch");
                            break;
                    }
                }
            }
        } finally {
            clientSocket.close();
        }
    }

    private String request(String[] msg) {
        // Check if the array has at least 4 elements
        if (msg.length > 3) {
            // Use StringBuilder for efficient string concatenation
            StringBuilder sb = new StringBuilder();
            // Iterate from the fourth element to the end of the array
            for (int i = 3; i < msg.length; i++) {
                sb.append(msg[i]);
                // Add a delimiter if not the last element
                if (i < msg.length - 1) {
                    sb.append(";");
                }
            }
            return sb.toString();
        } else {
            // Return an empty string or null based on what is appropriate for your application
            return "";
        }
    }

    private void sendAutenticar(String msg) {
        this.servicos.get(0).sendMessage(msg);
    }

    private void sendLoja(String msg) {
        this.servicos.get(1).sendMessage(msg);
    }

    private void sendToGateway(String mensagem) {
        ClientSocket sendGateway;
        try {
            sendGateway = new ClientSocket(new Socket("localhost", GATEWAY_PORTA));
            sendGateway.sendMessage(mensagem);
            sendGateway.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}