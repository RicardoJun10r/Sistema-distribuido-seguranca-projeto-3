package db;

import model.Cliente;
import model.Funcionario;
import model.Veiculo;
import security.CifrasSimetricas;
import util.Categoria;
import util.ClientSocket;
import util.HashTable.Table;

import java.time.LocalDate;
import java.io.IOException;
import java.lang.NullPointerException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;
import java.net.InetSocketAddress;

public class ReplicaBancoDeDados2 {

    private final String ENDERECO_SERVER = "localhost";
 
    private final int PORTA = 6157;

    private final int PORTA_PROXIMO2 = 6156;

    private final int PORTA_PROXIMO3 = 6158;

    private final int AUTENTICAR_SERVICO_PORTA = 1050;

    private final int LOJA_SERVICO_PORTA = 1061;

    private Table<Cliente, Integer> clientes;

    private Table<Funcionario, Integer> funcionarios;

    private Table<Veiculo, Integer> veiculos;

    private ServerSocket serverSocket;

    private CifrasSimetricas cifrasSimetricas;

    public ReplicaBancoDeDados2() {
        cifrasSimetricas = new CifrasSimetricas();
        this.clientes = new Table<>();
        this.veiculos = new Table<>();
        this.funcionarios = new Table<>();
        this.clientes.Adicionar(new Cliente("1234", cifrasSimetricas.hashPassword("123")), Integer.parseInt("1234"));
        this.clientes.Adicionar(new Cliente("4567", cifrasSimetricas.hashPassword("456")), Integer.parseInt("4567"));
        this.clientes.Adicionar(new Cliente("7891", cifrasSimetricas.hashPassword("789")), Integer.parseInt("7891"));
        this.funcionarios.Adicionar(new Funcionario("1047", cifrasSimetricas.hashPassword("147")), Integer.parseInt("1047"));
        this.funcionarios.Adicionar(new Funcionario("2058", cifrasSimetricas.hashPassword("258")), Integer.parseInt("2058"));
        this.funcionarios.Adicionar(new Funcionario("3069", cifrasSimetricas.hashPassword("369")), Integer.parseInt("3069"));
        this.veiculos.Adicionar(new Veiculo("1111", "Gol", Categoria.ECONOMICO, LocalDate.of(2022, 1, 1), 25000.0),
                Integer.parseInt("1111"));
        this.veiculos.Adicionar(new Veiculo("2222", "Palio", Categoria.ECONOMICO, LocalDate.of(2021, 12, 15), 22000.0),
                Integer.parseInt("2222"));
        this.veiculos.Adicionar(new Veiculo("3333", "Onix", Categoria.ECONOMICO, LocalDate.of(2022, 2, 10), 28000.0),
                Integer.parseInt("3333"));
        this.veiculos.Adicionar(
                new Veiculo("4444", "Civic", Categoria.INTERMEDIARIO, LocalDate.of(2022, 3, 5), 35000.0),
                Integer.parseInt("4444"));
        this.veiculos.Adicionar(
                new Veiculo("5555", "Corolla", Categoria.INTERMEDIARIO, LocalDate.of(2021, 11, 20), 38000.0),
                Integer.parseInt("5555"));
        this.veiculos.Adicionar(new Veiculo("6666", "Fiesta", Categoria.ECONOMICO, LocalDate.of(2022, 4, 12), 20000.0),
                Integer.parseInt("6666"));
        this.veiculos.Adicionar(
                new Veiculo("7777", "Cruze", Categoria.INTERMEDIARIO, LocalDate.of(2022, 2, 28), 32000.0),
                Integer.parseInt("7777"));
        this.veiculos.Adicionar(new Veiculo("8888", "Fit", Categoria.ECONOMICO, LocalDate.of(2022, 5, 8), 27000.0),
                Integer.parseInt("8888"));
        this.veiculos.Adicionar(
                new Veiculo("9999", "Fusion", Categoria.INTERMEDIARIO, LocalDate.of(2021, 10, 10), 40000.0),
                Integer.parseInt("9999"));
        this.veiculos.Adicionar(new Veiculo("7842", "HB20", Categoria.ECONOMICO, LocalDate.of(2022, 6, 20), 23000.0),
                Integer.parseInt("7842"));
        this.veiculos.Adicionar(
                new Veiculo("1112", "Ecosport", Categoria.EXECUTIVO, LocalDate.of(2022, 7, 15), 30000.0),
                Integer.parseInt("1112"));
        this.veiculos.Adicionar(new Veiculo("1212", "HR-V", Categoria.EXECUTIVO, LocalDate.of(2021, 9, 5), 290000.0),
                Integer.parseInt("1212"));
    }

    public void database() throws IOException {
        while (true) {
            ClientSocket clientSocket = new ClientSocket(this.serverSocket.accept());
            new Thread(() -> {
                queries(clientSocket);
            }).start();
        }
    }

    private void queries(ClientSocket clientSocket) {
        String mensagem;
        try {
            while ((mensagem = clientSocket.getMessage()) != null) {
                String[] msg = mensagem.split(";");
                System.out.println("Mensagem recebida de [ " + clientSocket.getSocketAddress() + " ] = " + mensagem);
                switch (msg[0]) {
                    case "att": {
                        switch (msg[1]) {
                            case "funcionario": {
                                insertFuncionario(msg[3], msg[4]);
                                break;
                            }
                            case "cliente": {
                                insertCliente(msg[3], msg[4]);
                                break;
                            }
                            case "veiculos": {
                                switch (msg[2]) {
                                    case "insert": {
                                        Categoria nova = setCategoria(msg[5]);
                                        Veiculo novo_veiculo = new Veiculo(msg[3], msg[4], nova,
                                                LocalDate.parse(msg[6]), Double.valueOf(msg[7]));
                                        insertVeiculo(novo_veiculo);
                                        break;
                                    }
                                    case "update": {
                                        if (msg[3].equals("compra")) {
                                            comprarVeiculo(msg[5], msg[4]);
                                        } else {
                                            Categoria nova = setCategoria(msg[5]);
                                            Veiculo veiculo_atualizado = new Veiculo(msg[3], msg[4], nova,
                                                    LocalDate.parse(msg[6]), Double.valueOf(msg[7]));
                                            updateVeiculo(veiculo_atualizado);
                                        }
                                        break;
                                    }
                                    case "delete": {
                                        deleteVeiculo(msg[3]);
                                        break;
                                    }
                                    default:
                                        break;
                                }
                                break;
                            }
                            default:
                                System.out.println("Erro ao atualizar BD");
                                break;
                        }
                        break;
                    }
                    case "funcionario": {
                        switch (msg[1]) {
                            case "select": {
                                if(msg[2].equals("boss")){
                                    sendToAutenticarServico("response;boss;" + selectAllFunc() + ";" + 1048);
                                } else if (selectFuncionario(msg[2], msg[3]) != null) {
                                    sendToAutenticarServico("response;login;true;" + msg[4]);
                                } else {
                                    sendToAutenticarServico("response;login;false;" + msg[4]);
                                }
                                break;
                            }
                            case "insert": {
                                insertFuncionario(msg[2], msg[3]);
                                attNextDb(mensagem);
                                sendToAutenticarServico("response;criado;" + msg[4]);
                                break;
                            }
                            default:
                                System.out.println("ERRO[BancoDeDados-funcionario]: " + mensagem);
                                break;
                        }
                        break;
                    }
                    case "cliente": {
                        switch (msg[1]) {
                            case "select": {
                                System.out.println("select: " + mensagem);
                                if (selectCliente(msg[2], msg[3]) != null) {
                                    System.out.println("response;login;true;" + msg[4]);
                                    sendToAutenticarServico("response;login;true;" + msg[4]);
                                } else {
                                    System.out.println("response;login;false;" + msg[4]);
                                    sendToAutenticarServico("response;login;false;" + msg[4]);
                                }
                                break;
                            }
                            case "insert": {
                                insertCliente(msg[2], msg[3]);
                                attNextDb(mensagem);
                                sendToAutenticarServico("response;criado;" + msg[4]);
                                break;
                            }
                            default:
                                System.out.println("ERRO[BancoDeDados-cliente]: " + mensagem);
                                break;
                        }
                        break;
                    }
                    case "veiculos": {
                        switch (msg[1]) {
                            case "select": {
                                if(msg[2].equals("quantidade")){
                                    sendToLojaServico("response;buscado;" + quantidadeCarros() + ";" + msg[3]);
                                } else if (msg[2].equals("-1")) {
                                    String response = "response;buscado;" + selectAllVeiculos() + ";" + msg[3];
                                    System.out.println("enviando do db para o loja service: " + response);
                                    sendToLojaServico(response);
                                } else {
                                    sendToLojaServico("response;buscado;" + selectVeiculo(msg[2]) + ";" + msg[3]);
                                }
                                break;
                            }
                            case "insert": {
                                Categoria nova = setCategoria(msg[4]);
                                Veiculo novo_veiculo = new Veiculo(msg[2], msg[3], nova,
                                        LocalDate.parse(msg[5]), Double.valueOf(msg[6]));
                                insertVeiculo(novo_veiculo);
                                attNextDb(mensagem);
                                sendToLojaServico("response;criado;" + msg[7]);
                                break;
                            }
                            case "update": {
                                if (msg[2].equals("compra")) {
                                    comprarVeiculo(msg[4], msg[3]);
                                    attNextDb(mensagem);
                                    sendToLojaServico("response;comprado;" + msg[5]);
                                } else {
                                    Categoria nova = setCategoria(msg[4]);
                                    Veiculo veiculo_atualizado = new Veiculo(msg[2], msg[3], nova,
                                            LocalDate.parse(msg[5]), Double.valueOf(msg[6]));
                                    updateVeiculo(veiculo_atualizado);
                                    attNextDb(mensagem);
                                    sendToLojaServico("response;atualizado;" + msg[7]);
                                }
                                break;
                            }
                            case "delete": {
                                deleteVeiculo(msg[2]);
                                attNextDb(mensagem);
                                sendToLojaServico("response;deletado;" + msg[3]);
                                break;
                            }
                            default:
                                System.out.println("ERRO[BancoDeDados-veiculos]: " + mensagem);
                                break;
                        }
                        break;
                    }
                    default:
                        System.out.println("Erro[BancoDeDados]: " + mensagem);
                        break;
                }
            }
        } finally {
            clientSocket.close();
        }
    }

    private ClientSocket tryConnect(int porta){
        try {
            Socket proximo = new Socket();
            proximo.connect(new InetSocketAddress(ENDERECO_SERVER, porta), 5*1000);
            return new ClientSocket(proximo);
        } catch (Exception e) {
            System.out.println("Erro: " + e);
        }
        return null;
    }

    private void attNextDb(String req){
        ClientSocket response = tryConnect(PORTA_PROXIMO2);
        response.sendMessage("att;" + req);
        response.close();
        response = tryConnect(PORTA_PROXIMO3);
        response.sendMessage("att;" + req);
        response.close();
    }

    private void sendToAutenticarServico(String mensagem) {
        ClientSocket response;
        try {
            response = new ClientSocket(new Socket("localhost", AUTENTICAR_SERVICO_PORTA));
            response.sendMessage(mensagem);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToLojaServico(String mensagem) {
        ClientSocket response;
        try {
            response = new ClientSocket(new Socket("localhost", LOJA_SERVICO_PORTA));
            response.sendMessage(mensagem);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Funcionario selectFuncionario(String login, String password) {
        try {
            Funcionario funcionario = this.funcionarios.BuscarCF(Integer.parseInt(login)).getValor();
            if (funcionario.getSenha().equals(this.cifrasSimetricas.hashPassword(password))) {
                return funcionario;
            } else {
                return null;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Cliente selectCliente(String login, String password) {
        try {
            Cliente cliente = this.clientes.BuscarCF(Integer.parseInt(login)).getValor();
            if (cliente.getSenha().equals(this.cifrasSimetricas.hashPassword(password))) {
                return cliente;
            } else {
                return null;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Veiculo selectVeiculo(String renavam) {
        try {
            Veiculo veiculo = this.veiculos.BuscarCF(Integer.parseInt(renavam)).getValor();
            if (veiculo != null) {
                return veiculo;
            } else {
                return null;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Categoria setCategoria(String categoria) {
        switch (categoria) {
            case "1":
                return Categoria.ECONOMICO;
            case "2":
                return Categoria.INTERMEDIARIO;
            case "3":
                return Categoria.EXECUTIVO;
            default:
                return Categoria.ECONOMICO;
        }
    }

    private String selectAllFunc() {
        return this.funcionarios.toStream()
                .map(Funcionario::toString)
                .collect(Collectors.joining("*"));
    }

    private String selectAllVeiculos() {
        return this.veiculos.toStream()
                .filter(veiculo -> veiculo.getA_venda() || veiculo.getCliente() == null)
                .map(Veiculo::toString)
                .collect(Collectors.joining("*"));
    }

    private void insertFuncionario(String login, String password) {
        this.funcionarios.Adicionar(new Funcionario(login, cifrasSimetricas.hashPassword(password)), Integer.parseInt(login));
    }

    private void insertCliente(String login, String password) {
        this.clientes.Adicionar(new Cliente(login, cifrasSimetricas.hashPassword(password)), Integer.parseInt(login));
    }

    private void insertVeiculo(Veiculo veiculo) {
        this.veiculos.Adicionar(veiculo, Integer.parseInt(veiculo.getRenavam()));
    }

    private void updateVeiculo(Veiculo novo) {
        try {
            this.veiculos.Atualizar(novo, Integer.parseInt(novo.getRenavam()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void comprarVeiculo(String renavam, String cpf) {
        try {
            Cliente cliente = this.clientes.BuscarCF(Integer.parseInt(cpf)).getValor();
            Veiculo veiculo = this.veiculos.BuscarCF(Integer.parseInt(renavam)).getValor();
            veiculo.setA_venda(false);
            cliente.setVeiculo(veiculo);
            veiculo.setCliente(cliente);
            this.clientes.Atualizar(cliente, Integer.parseInt(cpf));
            this.veiculos.Atualizar(veiculo, Integer.parseInt(renavam));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int quantidadeCarros(){
        return (int) this.veiculos.toStream().filter(carro -> carro.getA_venda()).count();
    }

    private void deleteVeiculo(String renavam) {
        try {
            this.veiculos.Remover(Integer.parseInt(renavam));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        this.serverSocket = new ServerSocket(PORTA);
        database();
    }

}
