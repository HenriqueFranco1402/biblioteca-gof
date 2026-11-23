package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.dominio.pessoa.Pessoa;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.util.Optional;

/** PADRAO TEMPLATE METHOD - Classe concreta: cadastro de Pessoa (diagrama "manterPessoa"). */
public class CadastroDePessoa extends CadastroTemplate<Dados.Pessoa, Pessoa> {

    private final Relacionamento relacionamento;

    public CadastroDePessoa(UnidadeDeTrabalho uow, Relacionamento relacionamento) {
        super(uow);
        this.relacionamento = relacionamento;
    }

    @Override
    protected Optional<Pessoa> buscarExistente(Dados.Pessoa d) {
        return relacionamento.consultarPessoa(d.cpf());          // 2: consultarPessoa(cpfPessoa)
    }

    @Override
    protected void validar(Dados.Pessoa d) {
        exigir(d.nome(), "nome");
        exigir(d.sobrenome(), "sobrenome");
        Pessoa.normalizarCpf(d.cpf());
    }

    @Override
    protected Pessoa criar(Dados.Pessoa d) {                      // 3: criar Pessoa
        return new Pessoa(d.cpf(), d.nome(), d.sobrenome(), d.email());
    }

    @Override
    protected void armazenar(Pessoa pessoa) {                     // 4: inserir(pessoa)
        relacionamento.inserirPessoa(pessoa);
    }

    @Override
    protected ResultadoCadastro apresentar(Pessoa p, boolean novo) {   // 5: mostrarDadosPessoa
        return new ResultadoCadastro(p.getCpfPessoa(), novo, p.getNomeCompleto() + " (CPF " + p.getCpfPessoa() + ")");
    }
}
