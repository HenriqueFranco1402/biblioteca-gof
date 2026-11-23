package br.pucpr.biblioteca.cadastro;

/** Resultado do metodo-modelo: o id, se foi criado agora (novo) ou se ja existia, e uma descricao. */
public record ResultadoCadastro(String id, boolean novo, String descricao) {

    public String resumo() {
        return (novo ? "Cadastrado: " : "Ja existia (cadastro existente): ") + descricao;
    }
}
