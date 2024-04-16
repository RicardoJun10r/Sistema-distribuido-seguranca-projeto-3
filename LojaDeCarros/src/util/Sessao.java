package util;


import security.CifrasSimetricas;
import security.RSA;

public class Sessao {
    
    private Boolean logado;
    
    private Boolean admin;

    private RSA rsa;

    private CifrasSimetricas seguranca;
    
    public Sessao(Boolean logado, Boolean admin) {
        this.logado = logado;
        this.admin = admin;
        this.rsa = new RSA();
        this.seguranca = new CifrasSimetricas();
    }
    
    public CifrasSimetricas getSeguranca() {
        return seguranca;
    }

    public void setSeguranca(CifrasSimetricas seguranca) {
        this.seguranca = seguranca;
    }

    public RSA getRsa() {
        return rsa;
    }

    public void setRsa(RSA rsa) {
        this.rsa = rsa;
    }

    public Boolean getLogado() {
        return logado;
    }

    public void setLogado(Boolean logado) {
        this.logado = logado;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

}
