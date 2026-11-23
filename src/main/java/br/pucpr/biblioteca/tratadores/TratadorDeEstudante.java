package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.cadastro.CadastroDeEstudante;
import br.pucpr.biblioteca.cadastro.Dados;
import br.pucpr.biblioteca.cadastro.ResultadoCadastro;
import br.pucpr.biblioteca.componentes.iEstudante;

import java.util.Optional;

/** Tratador de estudantes (componente Academico, implementa {@link iEstudante}). */
public class TratadorDeEstudante implements iEstudante {

    private final CadastroDeEstudante cadastro;
    private final Relacionamento relacionamento;

    public TratadorDeEstudante(CadastroDeEstudante cadastro, Relacionamento relacionamento) {
        this.cadastro = cadastro;
        this.relacionamento = relacionamento;
    }

    @Override
    public ResultadoCadastro incluirEstudante(Dados.Estudante dados) {
        return cadastro.incluir(dados);
    }

    @Override
    public Optional<String> consultarEstudante(String idEstudante) {
        return relacionamento.consultarEstudante(idEstudante)
                .map(e -> e.getIdEstudante() + " | " + e.getNomeCompleto() + " | " + e.getEmail());
    }
}
