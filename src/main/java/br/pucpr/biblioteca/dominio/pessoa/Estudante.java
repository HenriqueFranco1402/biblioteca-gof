package br.pucpr.biblioteca.dominio.pessoa;

/** Estudante (componente Academico). Especializa Pessoa e e sempre associado a um CPF ja cadastrado. */
public class Estudante extends Pessoa {
    private final String idEstudante;

    public Estudante(String idEstudante, Pessoa pessoa) {
        this(idEstudante, pessoa.getCpfPessoa(), pessoa.getNome(), pessoa.getSobrenome(), pessoa.getEmail());
    }

    public Estudante(String idEstudante, String cpf, String nome, String sobrenome, String email) {
        super(cpf, nome, sobrenome, email);
        this.idEstudante = idEstudante;
    }

    public String getIdEstudante() { return idEstudante; }
}
