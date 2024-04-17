package model;

public class Funcionario extends Usuario {

    public Funcionario(String cpf, String senha){
        super(cpf, senha);
    }

    @Override
    public String toString() {
        return "Funcionario [cpf=" + super.getCpf() + "], [senha=" + super.getSenha() + "]";
    }
    
}
