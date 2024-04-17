package microsservice.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import util.ClientSocket;

public class ReplicaLoja2 {
    
    public final int PORTA = 1061;

    public final int FIREWALL_PORTA = 10101;

    public final int DATABASE_PORTA = 6157;

    private final String ENDERECO_SERVER = "localhost";

    private ServerSocket serverSocket;

    public ReplicaLoja2() {
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORTA);
        System.out.println("Iniciando serviço na porta = " + PORTA);
        lojaService();
    }

    private void lojaService() throws IOException {
        while (true) {
            ClientSocket clientSocket = new ClientSocket(this.serverSocket.accept());
            new Thread(() -> {
                try {
                    lojaLoop(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void lojaLoop(ClientSocket clientSocket) throws IOException {
        String mensagem;
        try {
            while ((mensagem = clientSocket.getMessage()) != null) {
                System.out.println("Mensagem de [ " + clientSocket.getSocketAddress() + " ] = " + mensagem);
                String[] msg = mensagem.split(";");
                if (msg[0].equals("response")) {
                    switch (msg[1]) {
                        case "buscado": {
                            sendToFirewall("loja;servico;lista;" + msg[2] + ";" + msg[3]);
                            break;
                        }
                        case "criado": {
                            sendToFirewall("loja;servico;Carro adicionado!;" + msg[2]);
                            break;
                        }
                        case "comprado": {
                            sendToFirewall("loja;servico;Carro comprado!;" + msg[2]);
                            break;
                        }
                        case "atualizado": {
                            sendToFirewall("loja;servico;Carro atualizado!;" + msg[2]);
                            break;
                        }
                        case "deletado": {
                            sendToFirewall("loja;servico;Carro deletado!;" + msg[2]);
                            break;
                        }
                        default:
                            System.out.println("Erro[LojaService-response]: " + mensagem);
                            break;
                    }
                } else {
                    switch (msg[1]) {
                        case "3": {
                            if (Boolean.parseBoolean(msg[0])) {
                                sendToDB("veiculos;insert;" + msg[2] + ";" + msg[3] + ";" + msg[4] + ";" + msg[5] + ";"
                                        + msg[6] + ";" + msg[7]);
                            } else {
                                sendToFirewall("loja;servico;Erro: SEM AUTORIZACAO;" + msg[7]);
                            }
                            break;
                        }
                        case "4": {
                            sendToDB("veiculos;select;" + msg[2] + ";" + msg[3]);
                            break;
                        }
                        case "5": {
                            sendToDB("veiculos;select;-1;" + msg[2]);
                            break;
                        }
                        case "6": {
                            sendToDB("veiculos;select;quantidade;" + msg[2]);
                            break;
                        }
                        case "7": {
                            sendToDB("veiculos;update;compra;" + msg[2] + ";" + msg[3] + ";" + msg[4]);
                            break;
                        }
                        case "8": {
                            if (Boolean.parseBoolean(msg[0])) {
                                sendToDB("veiculos;delete;" + msg[2] + ";" + msg[3]);
                            } else {
                                sendToFirewall("loja;servico;Erro: SEM AUTORIZACAO;" + msg[3]);
                            }
                            break;
                        }
                        case "9": {
                            if (Boolean.parseBoolean(msg[0])) {
                                sendToDB("veiculos;update;" + msg[2] + ";" + msg[3] + ";" + msg[4] + ";" + msg[5] + ";"
                                        + msg[6] + ";" + msg[7]);
                            } else {
                                sendToFirewall("loja;servico;Erro: SEM AUTORIZACAO;" + msg[7]);
                            }
                            break;
                        }
                        default:
                            break;
                    }
                }
            }
        } finally {
            clientSocket.close();
        }
    }

    private void sendToFirewall(String mensagem) {
        ClientSocket sendFirewall;
        try {
            sendFirewall = new ClientSocket(new Socket("localhost", FIREWALL_PORTA));
            sendFirewall.sendMessage(this.ENDERECO_SERVER + ";" + this.PORTA + ";" + 1042 + ";" + mensagem);
            sendFirewall.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToDB(String req) {
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
