package br.pucpr.biblioteca.dominio.pessoa;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

import java.util.Objects;

/** Pessoa cadastrada no RH (diagrama de classes: nome, sobrenome, cpfPessoa). O e-mail foi acrescentado para os avisos. */
public class Pessoa {
    private final String cpfPessoa;
    private String nome;
    private String sobrenome;
    private String email;

    public Pessoa(String cpfPessoa, String nome, String sobrenome, String email) {
        this.cpfPessoa = normalizarCpf(cpfPessoa);
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
    }

    /** Mantem apenas os digitos. Validacao basica de formato (11 digitos). */
    public static String normalizarCpf(String cpf) {
        String digitos = Objects.requireNonNullElse(cpf, "").replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new RegraDeNegocioException("CPF invalido: " + cpf);
        }
        return digitos;
    }

    public String getCpfPessoa() { return cpfPessoa; }

    public String getNome() { return nome; }

    public String getSobrenome() { return sobrenome; }

    public String getEmail() { return email; }

    public String getNomeCompleto() { return nome + " " + sobrenome; }

    public void atualizarDados(String nome, String sobrenome, String email) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
    }
}
