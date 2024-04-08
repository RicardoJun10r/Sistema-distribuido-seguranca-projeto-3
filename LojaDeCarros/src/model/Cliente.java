package model;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Usuario {

    private List<Veiculo> veiculos;
    
    public Cliente(String cpf, String senha) {
        super(cpf, senha);
        this.veiculos = new ArrayList<>();
    }

    public List<Veiculo> getVeiculo() {
        return veiculos;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculos.add(veiculo);
    }

    @Override
    public String toString() {
        String carros = "";
        for(Veiculo index : veiculos){
            carros += index.toString() + "\n";
        }
        return "Cliente [veiculos=" + carros + "]";
    }

}
