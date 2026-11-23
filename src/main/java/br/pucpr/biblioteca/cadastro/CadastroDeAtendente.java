package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.dominio.pessoa.Atendente;
import br.pucpr.biblioteca.dominio.pessoa.Pessoa;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.util.Optional;

/**
 * PADRAO TEMPLATE METHOD - Classe concreta: cadastro de Atendente (diagrama "manterAtendente").
 * Sobrescreve o GANCHO: "pessoa nao cadastrada" impede o cadastro.
 */
public class CadastroDeAtendente extends CadastroTemplate<Dados.Atendente, Atendente> {

    private final Relacionamento relacionamento;

    public CadastroDeAtendente(UnidadeDeTrabalho uow, Relacionamento relacionamento) {
        super(uow);
        this.relacionamento = relacionamento;
    }

    @Override
    protected Optional<Atendente> buscarExistente(Dados.Atendente d) {
        return d.idAtendente() == null ? Optional.empty() : relacionamento.consultarAtendente(d.idAtendente());
    }

    @Override
    protected void validar(Dados.Atendente d) {
        exigir(d.idAtendente(), "idAtendente");
        Pessoa.normalizarCpf(d.cpf());
    }

    @Override
    protected void verificarPreRequisitos(Dados.Atendente d) {
        if (relacionamento.consultarPessoa(d.cpf()).isEmpty()) {
            throw new PreRequisitoNaoAtendidoException("Pessoa nao cadastrada: CPF " + d.cpf());
        }
    }

    @Override
    protected Atendente criar(Dados.Atendente d) {
        return new Atendente(d.idAtendente(), relacionamento.consultarPessoa(d.cpf()).orElseThrow());
    }

    @Override
    protected void armazenar(Atendente atendente) {
        relacionamento.inserirAtendente(atendente);
    }

    @Override
    protected ResultadoCadastro apresentar(Atendente a, boolean novo) {
        return new ResultadoCadastro(a.getIdAtendente(), novo,
                "Atendente " + a.getIdAtendente() + " - " + a.getNomeCompleto());
    }
}
