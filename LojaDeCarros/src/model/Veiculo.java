package model;

import java.io.Serializable;
import java.time.LocalDate;

import util.Categoria;

public class Veiculo implements Serializable {

    // ID
    private String renavam;

    private String nome;

    private Categoria categoria;

    private LocalDate criado_em;

    private Double preco;

    private Cliente cliente;

    private Boolean a_venda;
    
    public Veiculo(String renavam, String nome, Categoria categoria, LocalDate criado_em, Double preco) {
        this.renavam = renavam;
        this.nome = nome;
        this.categoria = categoria;
        this.criado_em = criado_em;
        this.preco = preco;
        this.a_venda = true;
        this.cliente = null;
    }
    
    public Boolean getA_venda() {
        return a_venda;
    }

    public void setA_venda(Boolean a_venda) {
        this.a_venda = a_venda;
    }

    public String getRenavam() {
        return renavam;
    }

    public void setRenavam(String renavam) {
        this.renavam = renavam;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public LocalDate getCriado_em() {
        return criado_em;
    }

    public void setCriado_em(LocalDate criado_em) {
        this.criado_em = criado_em;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    @Override
    public String toString() {
        return "Veiculo [renavam=" + renavam + ", nome=" + nome + ", categoria=" + categoria + ", criado_em="
                + criado_em + ", preco=" + preco + ", a venda=" + a_venda + "]";
    }

}
