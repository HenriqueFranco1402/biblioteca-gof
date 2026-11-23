package br.pucpr.biblioteca.dominio.pessoa;

/** Atendente (componente RH). Especializa Pessoa e e sempre associado a um CPF ja cadastrado. */
public class Atendente extends Pessoa {
    private final String idAtendente;

    public Atendente(String idAtendente, Pessoa pessoa) {
        this(idAtendente, pessoa.getCpfPessoa(), pessoa.getNome(), pessoa.getSobrenome(), pessoa.getEmail());
    }

    public Atendente(String idAtendente, String cpf, String nome, String sobrenome, String email) {
        super(cpf, nome, sobrenome, email);
        this.idAtendente = idAtendente;
    }

    public String getIdAtendente() { return idAtendente; }
}
