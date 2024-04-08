package util;

public class Sessao {
    
    private Boolean logado;
    
    private Boolean admin;
    
    public Sessao(Boolean logado, Boolean admin) {
        this.logado = logado;
        this.admin = admin;
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
