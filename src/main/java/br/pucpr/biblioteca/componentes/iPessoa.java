package br.pucpr.biblioteca.componentes;

import br.pucpr.biblioteca.cadastro.Dados;
import br.pucpr.biblioteca.cadastro.ResultadoCadastro;

import java.util.Optional;

/** Interface do componente RH para pessoas (diagrama de componentes). */
public interface iPessoa {

    ResultadoCadastro incluirPessoa(Dados.Pessoa dados);

    Optional<String> consultarPessoa(String cpf);
}
