package microsservice.gateway;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import util.ClientSocket;
import util.Sessao;

public class Gateway {

    public final int PORTA = 1042;

    private final String ENDERECO_SERVER = "localhost";

    private final int FIREWALL_PORTA = 10101;

    private ServerSocket serverSocket;

    private ClientSocket FIREWALL;

    private final List<ClientSocket> USUARIOS = new LinkedList<>();

    private final Map<SocketAddress, Sessao> SESSAO = new HashMap<>();

    public Gateway() {
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORTA);
        System.out.println("Iniciando servidor na porta = " + PORTA);
        this.FIREWALL = (new ClientSocket(new Socket(ENDERECO_SERVER, FIREWALL_PORTA)));
        System.out.println("Conectado ao FIREWALL ...");
        clientConnectionLoop();
    }

    private void clientConnectionLoop() throws IOException {
        while (true) {
            ClientSocket clientSocket = new ClientSocket(this.serverSocket.accept());
            USUARIOS.add(clientSocket);
            this.SESSAO.put(clientSocket.getSocketAddress(), new Sessao(false, false));
            new Thread(() -> {
                try {
                    gatewayLoop(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private Boolean autenticar(ClientSocket clientSocket) {
        if (this.SESSAO.get(clientSocket.getSocketAddress()).getLogado())
            return true;
        else
            return false;
    }

    private void gatewayLoop(ClientSocket clientSocket) throws IOException {
        String mensagem;
        String hmac = "";
        Sessao sessao = this.SESSAO.get(clientSocket.getSocketAddress());
        try {
            while ((mensagem = clientSocket.getMessage()) != null) {
                System.out.println("Mensagem de [ " + clientSocket.getSocketAddress() + " ] = " + mensagem);
                if (mensagem.split(";")[0].equals("rsa_chaves")) {
                    System.out.println("Pegando chaves RSA");
                    sessao.getRsa().setP(Long.parseLong(mensagem.split(";")[1]));
                    sessao.getRsa().setQ(Long.parseLong(mensagem.split(";")[2]));
                    sessao.getRsa().setE(Long.parseLong(mensagem.split(";")[3]));

                    sessao.getRsa().setN(sessao.getRsa().getP() * sessao.getRsa().getQ());
                    sessao.getRsa().phi(sessao.getRsa().getP(), sessao.getRsa().getQ());
                    sessao.getRsa().expD(sessao.getRsa().getE(), sessao.getRsa().getPhi());

                    sessao.getRsa().gerarE_estrangeiro();
                    this.SESSAO.put(clientSocket.getSocketAddress(), sessao);
                    this.SESSAO.get(clientSocket.getSocketAddress()).getSeguranca().setChaveVernan(mensagem.split(";")[4]);
                    unicast(clientSocket, "rsa " + sessao.getRsa().getE_extrangeiro());
                } else {
                    String[]msg = mensagem.split(";");
                    if (msg[0].equals("autenticar")) {
                        if (msg[1].equals("servico")) {

                            // RESPOSTA DO SERCIÇO

                            System.out.println(
                                    "[autenticar-servico] Mensagem de " + clientSocket.getSocketAddress() + ": "
                                            + mensagem);
                            if (msg[2].equals("login")) {
                                SocketAddress socketAddress = this.USUARIOS.stream()
                                        .filter(conexoes -> conexoes.getSocketAddress().toString().equals(msg[4]))
                                        .findFirst()
                                        .get().getSocketAddress();
                                sessao = this.SESSAO.get(socketAddress);
                                if (Boolean.parseBoolean(msg[3])) {
                                    sessao.setLogado(true);
                                    this.SESSAO.put(socketAddress, sessao);
                                    String aes = sessao.getSeguranca().cifrar("status true");
                                    String msg_rsa = sessao.getRsa().cifragemServer(aes);
                                    unicast_with_string(msg[4], msg_rsa);
                                } else {
                                    this.SESSAO.put(socketAddress, new Sessao(false, false));
                                    String aes = sessao.getSeguranca().cifrar("status false");
                                    String msg_rsa = sessao.getRsa().cifragemServer(aes);
                                    unicast_with_string(msg[4], msg_rsa);
                                }
                            } else if (msg[2].equals("criado")) {
                                String aes = sessao.getSeguranca().cifrar("Conta criada!");
                                String msg_rsa = sessao.getRsa().cifragemServer(aes);
                                unicast_with_string(msg[3], msg_rsa);
                            }
                        } else if (msg[1].equals("cliente")) {

                            // REQUISIÇÃO DO CLIENTE

                            String msg_aberta = sessao.getRsa().decifragemCliente(msg[2]);
                            System.out.println("MENSAGEM DECIFRADA RSA: " + msg_aberta);
                            
                            System.out.println(
                                    "[autenticar-cliente] Mensagem de " + clientSocket.getSocketAddress() + ": "
                                            + msg_aberta);
                            if (Boolean.parseBoolean(msg_aberta.split(";")[2])) {
                                this.SESSAO.put(clientSocket.getSocketAddress(), new Sessao(false, true));
                            }
                            if(msg_aberta.split(";")[1].equals("1")){
                                this.SESSAO.get(clientSocket.getSocketAddress()).getSeguranca()
                                        .setChave((SecretKey) clientSocket.receberObjeto());
                                System.out.println("Chave: "
                                        + this.SESSAO.get(clientSocket.getSocketAddress()).getSeguranca().getChave());
                            }
                            FIREWALL.sendMessage(this.ENDERECO_SERVER + ";" + this.PORTA + ";" + 1050 + ";" + msg_aberta
                                    + clientSocket.getSocketAddress().toString());
                        }
                    } else if (mensagem.split(";")[0].equals("loja")) {
                        if (mensagem.split(";")[1].equals("servico")) {
                            System.out.println(
                                    "[loja-servico] Mensagem de " + clientSocket.getSocketAddress() + ": " + mensagem);
                                    
                            if (mensagem.split(";")[2].equals("lista")) {
                                SocketAddress socketAddress = this.USUARIOS.stream()
                                        .filter(conexoes -> conexoes.getSocketAddress().toString().equals(msg[4]))
                                        .findFirst()
                                        .get().getSocketAddress();
                                sessao = this.SESSAO.get(socketAddress);
                                String aes = sessao.getSeguranca().cifrar("lista " + mensagem.split(";")[3]);
                                String msg_rsa = sessao.getRsa().cifragemServer(aes);
                                unicast_with_string(mensagem.split(";")[4], msg_rsa);
                            } else {
                                SocketAddress socketAddress = this.USUARIOS.stream()
                                        .filter(conexoes -> conexoes.getSocketAddress().toString().equals(msg[3]))
                                        .findFirst()
                                        .get().getSocketAddress();
                                sessao = this.SESSAO.get(socketAddress);
                                String aes = sessao.getSeguranca().cifrar(mensagem.split(";")[2]);
                                String msg_rsa = sessao.getRsa().cifragemServer(aes);
                                unicast_with_string(mensagem.split(";")[3], msg_rsa);
                            }
                        } else if (mensagem.split(";")[1].equals("cliente")) {
                            String msg_aberta = sessao.getRsa().decifragemCliente(msg[2]);
                            System.out.println("MENSAGEM DECIFRADA RSA: " + msg_aberta);
                            hmac = msg_aberta.split(";")[1];
                            sessao = this.SESSAO.get(clientSocket.getSocketAddress());
                            msg_aberta = sessao.getSeguranca().decifrar(msg_aberta.split(";")[0]);
                            if(autenticarMensagem(msg_aberta, hmac, sessao)){
                                System.out.println("PASSOU HMAC");
                                if (autenticar(clientSocket)) {
                                    System.out.println(
                                            "[loja-cliente] Mensagem de " + clientSocket.getSocketAddress() + ": "
                                                    + msg_aberta);
                                    FIREWALL.sendMessage(this.ENDERECO_SERVER + ";" + this.PORTA + ";" + 1060 + ";"
                                            + msg_aberta + ";" + clientSocket.getSocketAddress().toString());
                                } else {
                                    System.out.println(
                                            "[loja-cliente] Mensagem de " + clientSocket.getSocketAddress() + ": "
                                                    + msg_aberta);
                                    unicast(clientSocket, "ACESSO NEGADO!");
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            clientSocket.close();
        }
    }

    private Boolean autenticarMensagem(String mensagem, String hmac_recebido, Sessao sessao) {
        String hmac = sessao.getSeguranca().hMac(mensagem);
        if (hmac.equals(hmac_recebido))
            return true;
        else
            return false;
    }

    private void unicast(ClientSocket destinario, String mensagem) {
        ClientSocket emissor = this.USUARIOS.stream()
                .filter(user -> user.getSocketAddress().equals(destinario.getSocketAddress()))
                .findFirst().get();
        emissor.sendMessage(mensagem);
    }

    private void unicast_with_string(String destinario, String mensagem) {
        ClientSocket emissor = this.USUARIOS.stream()
                .filter(user -> user.getSocketAddress().toString().equals(destinario))
                .findFirst().get();
        emissor.sendMessage(mensagem);
    }

}
