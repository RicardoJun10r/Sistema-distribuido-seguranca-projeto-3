package client;

import util.ClientSocket;
import java.util.Scanner;

import security.CifrasSimetricas;
import security.RSA;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;

public class UserInterface implements Runnable {

    private final String ENDERECO_SERVER = "localhost";

    private final int GATEWAY_PORTA = 1042;

    private ClientSocket clientSocket;

    private Scanner scan;

    private Boolean logado;

    private final Boolean ADMIN;

    private CifrasSimetricas seguranca = new CifrasSimetricas(192);

    private RSA rsa;

    public UserInterface(Boolean admin) {
        this.scan = new Scanner(System.in);
        this.logado = false;
        this.ADMIN = admin;
        this.rsa = new RSA();
    }

    @Override
    public void run() {
        String mensagem;
        while ((mensagem = this.clientSocket.getMessage()) != null) {
            if (mensagem.split(" ")[0].equals("rsa")) {
                this.rsa.setE_extrangeiro(Long.parseLong(mensagem.split(" ")[1]));
                this.rsa.phi(this.rsa.getP(), this.rsa.getQ());
                this.rsa.expD(this.rsa.getE_extrangeiro(), this.rsa.getPhi());
                System.out.println(
                        "Resposta da loja: " + mensagem);
            } else {
                if (mensagem.split(" ")[0].equals("status")) {
                    logado = Boolean.parseBoolean(mensagem.split(" ")[1]);
                } else if (mensagem.split(" ")[0].equals("lista")) {
                    String res = mensagem.replace("*", "\n");
                    System.out.println("Resposta da loja: " + res);
                } else {
                    System.out.println(
                            "Resposta da loja: " + mensagem);
                }
            }
        }
    }

    private void autenticar() {
        System.out.println("> 1 Entrar\n> 2 Registrar-se");
        System.out.print("> ");
        String op = scan.next();
        if (op.equals("1")) {
            System.out.println("> CPF");
            System.out.print("> ");
            String login = scan.next();
            System.out.println("> Senha");
            System.out.print("> ");
            String senha = scan.next();
            String msg_rsa = this.rsa.cifragemCliente(ADMIN + ";1;" + login + ";" + senha + ";");
            System.out.println("RSA: " + msg_rsa);
            enviar("autenticar;cliente;" + msg_rsa);
            sendKey();
        } else if (op.equals("2")) {
            String senha;
            String nova_conta = "";
            System.out.println("Registrando\n> CPF");
            System.out.print("> ");
            nova_conta += scan.next() + ";";
            System.out.println("> Senha");
            System.out.print("> ");
            senha = scan.next();
            nova_conta += senha;
            enviar("autenticar;cliente;" + ADMIN + ";2;" + nova_conta + ";");
        }
    }

    private void enviar(String mensagem) {
        this.clientSocket.sendMessage(mensagem);
    }

    private void sendKey(){
        System.out.println("Chave: " + this.seguranca.getChave());
        this.clientSocket.enviarObjeto(this.seguranca.getChave());
    }

    private void menu() {
        if (ADMIN) {
            System.out.println(
                    "> 3 [ ADICIONAR CARRO ]\n> 4 [ BUSCAR CARRO ]\n> 5 [ LISTAR CARROS ]\n> 6 [ QUANTIDADE DE CARROS ]\n> 7 [ COMPRAR CARRO ]\n> 8 [ APAGAR CARRO ]\n> 9 [ ATUALIZAR CARRO ]\n> sair");
        } else {
            System.out.println(
                    "> 4 [ BUSCAR CARRO ]\n> 5 [ LISTAR CARROS ]\n> 6 [ QUANTIDADE DE CARROS ]\n> 7 [ COMPRAR CARRO ]\n> sair");
        }
    }

    private void messageLoop() {
        String mensagem = "";
        try {
            do {
                Thread.sleep(300);
                if (!logado) {
                    autenticar();
                } else {
                    System.out.println("> LOGADO");
                    menu();
                    System.out.print("> ");
                    mensagem = scan.next();
                    processOption(mensagem);
                }
            } while (!mensagem.equalsIgnoreCase("sair"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processOption(String option) {
        String msg;
        switch (option) {
            case "3":
                msg = ADMIN + ";3;";
                System.out.println("> RENAVAM");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> NOME");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> CATEGORIA\n> 1 [ ECONOMICO ]\n> 2 [ INTERMEDIARIO ]\n> 3 [ EXECUTIVO ]");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> DATA DE CRIAÇÃO [ ANO-MES-DIA ]");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> PREÇO");
                System.out.print("> R$ ");
                msg += this.scan.next();
                enviar("loja;cliente;" + msg);
                break;
            case "4":
                msg = ADMIN + ";4;";
                System.out.println("> RENAVAM");
                System.out.print("> ");
                msg += this.scan.next();
                enviar("loja;cliente;" + msg);
                break;
            case "5":
                msg = ADMIN + ";5";
                System.out.println("> LISTANDO...");
                enviar("loja;cliente;" + msg);
                break;
            case "6":
                msg = ADMIN + ";6";
                enviar("loja;cliente;" + msg);
                break;
            case "7":
                msg = ADMIN + ";7;";
                System.out.println("> CPF");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> RENAVAM");
                System.out.print("> ");
                msg += this.scan.next();
                enviar("loja;cliente;" + msg);
                break;
            case "8":
                msg = ADMIN + ";8;";
                System.out.println("> RENAVAM");
                System.out.print("> ");
                msg += this.scan.next();
                enviar("loja;cliente;" + msg);
                break;
            case "9":
                msg = ADMIN + ";9;";
                System.out.println("> RENAVAM");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> NOME OU * [ VAZIO ]");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println(
                        "> CATEGORIA OU * [ VAZIO ]\n> 1 [ ECONOMICO ]\n> 2 [ INTERMEDIARIO ]\n> 3 [ EXECUTIVO ]");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> DATA DE CRIAÇÃO [ ANO-MES-DIA ] OU * [ VAZIO ]");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> PREÇO OU * [ VAZIO ]");
                System.out.print("> R$ ");
                msg += this.scan.next();
                enviar("loja;cliente;" + msg);
                break;
            case "sair":
                System.out.println("Saindo");
                break;
            default:
                System.out.println("comando não achado");
                break;
        }
    }

    public void start() throws IOException, UnknownHostException {
        try {
            clientSocket = new ClientSocket(
                    new Socket(ENDERECO_SERVER, GATEWAY_PORTA));
            System.out
                    .println("Cliente conectado ao gateway de endereço = " + ENDERECO_SERVER + " na porta = " + GATEWAY_PORTA);
            new Thread(this).start();
            this.rsa.gerarPG();
            this.rsa.setN(this.rsa.getP()*this.rsa.getQ());
            this.rsa.gerarE();
            System.out.println("Enivando { p, q, e }");
            enviar("rsa_chaves;" + this.rsa.getP() + ";" + this.rsa.getQ() + ";" + this.rsa.getE());
            messageLoop();
        } finally {
            clientSocket.close();
        }
    }

}
