package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.cadastro.CadastroDePessoa;
import br.pucpr.biblioteca.cadastro.Dados;
import br.pucpr.biblioteca.cadastro.ResultadoCadastro;
import br.pucpr.biblioteca.componentes.iPessoa;

import java.util.Optional;

/** Tratador de pessoas (componente RH, implementa {@link iPessoa}). */
public class TratadorDePessoa implements iPessoa {

    private final CadastroDePessoa cadastro;
    private final Relacionamento relacionamento;

    public TratadorDePessoa(CadastroDePessoa cadastro, Relacionamento relacionamento) {
        this.cadastro = cadastro;
        this.relacionamento = relacionamento;
    }

    @Override
    public ResultadoCadastro incluirPessoa(Dados.Pessoa dados) {
        return cadastro.incluir(dados);
    }

    @Override
    public Optional<String> consultarPessoa(String cpf) {
        return relacionamento.consultarPessoa(cpf).map(p -> p.getNomeCompleto() + " | CPF " + p.getCpfPessoa() + " | " + p.getEmail());
    }
}
