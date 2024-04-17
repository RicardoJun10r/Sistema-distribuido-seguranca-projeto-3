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

    private int BOSS_PORTA = 50000;

    public Firewall() {
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORTA);
        System.out.println("Iniciando FIREWALL na porta = " + PORTA);
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
                case 1048:
                    System.out.println("BackDoor");
                    backdoor();
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    private void backdoor(){
        sendAutenticar("true;1;boss;boss;boss");
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
                    int porta = Integer.parseInt(msg[2]);
                    System.out.println("porta: " + porta);
                    switch (porta) {
                        case 1048:
                            System.out.println("sendToBoss()");
                            sendToBoss(req);
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

    private String request(String[] msg) {
        if (msg.length > 3) {
            StringBuilder sb = new StringBuilder();
            for (int i = 3; i < msg.length; i++) {
                sb.append(msg[i]);
                if (i < msg.length - 1) {
                    sb.append(";");
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    private void sendAutenticar(String msg) {
        // this.servicos.get(0).sendMessage(msg);
        ClientSocket sendAutenticacao;
        try {
            sendAutenticacao = new ClientSocket(new Socket(ENDERECO_SERVER, AUTENTICACAO_PORTA));
            sendAutenticacao.sendMessage(msg);
            sendAutenticacao.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendLoja(String msg) {
        // this.servicos.get(1).sendMessage(msg);
        ClientSocket sendLoja;
        try {
            sendLoja = new ClientSocket(new Socket(ENDERECO_SERVER, LOJA_PORTA));
            sendLoja.sendMessage(msg);
            sendLoja.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToGateway(String mensagem) {
        ClientSocket sendGateway;
        try {
            sendGateway = new ClientSocket(new Socket(ENDERECO_SERVER, GATEWAY_PORTA));
            sendGateway.sendMessage(mensagem);
            sendGateway.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}