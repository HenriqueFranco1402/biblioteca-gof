package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.cadastro.CadastroDeAtendente;
import br.pucpr.biblioteca.cadastro.Dados;
import br.pucpr.biblioteca.cadastro.ResultadoCadastro;
import br.pucpr.biblioteca.componentes.iAtendente;

import java.util.Optional;

/** Tratador de atendentes (componente RH, implementa {@link iAtendente}). */
public class TratadorDeAtendente implements iAtendente {

    private final CadastroDeAtendente cadastro;
    private final Relacionamento relacionamento;

    public TratadorDeAtendente(CadastroDeAtendente cadastro, Relacionamento relacionamento) {
        this.cadastro = cadastro;
        this.relacionamento = relacionamento;
    }

    @Override
    public ResultadoCadastro incluirAtendente(Dados.Atendente dados) {
        return cadastro.incluir(dados);
    }

    @Override
    public Optional<String> consultarAtendente(String idAtendente) {
        return relacionamento.consultarAtendente(idAtendente)
                .map(a -> a.getIdAtendente() + " | " + a.getNomeCompleto());
    }
}
