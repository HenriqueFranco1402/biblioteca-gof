package br.pucpr.biblioteca.componentes;

import br.pucpr.biblioteca.cadastro.Dados;
import br.pucpr.biblioteca.cadastro.ResultadoCadastro;

import java.util.Optional;

/** Interface do componente Academico para estudantes (diagrama de componentes). */
public interface iEstudante {

    ResultadoCadastro incluirEstudante(Dados.Estudante dados);

    Optional<String> consultarEstudante(String idEstudante);
}
